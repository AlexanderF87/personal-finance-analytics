package de.finance.analytics.transactions.service;

import de.finance.analytics.transactions.entity.Category;
import de.finance.analytics.transactions.entity.Transaction;
import de.finance.analytics.transactions.entity.TransactionState;
import de.finance.analytics.transactions.entity.TransactionType;
import de.finance.analytics.transactions.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 💰 TransactionService - Core Banking Transaction Business Logic
 *
 * Design Patterns:
 * - Repository Pattern: Data access abstraction
 * - Template Method: Common transaction processing workflows
 * - Command Pattern: Transaction operations
 *
 * Features:
 * - CRUD operations for transactions
 * - Financial analytics and reporting
 * - Batch processing support
 * - German banking conventions
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;

    /**
     * 💾 CRUD Operations
     */
    @Transactional
    public Transaction saveTransaction(Transaction transaction) {
        // Auto-categorize if no category assigned
        if (transaction.getCategory() == null) {
            categoryService.categorizeTransaction(transaction)
                    .ifPresent(transaction::setCategory);
        }

        Transaction saved = transactionRepository.save(transaction);
        log.debug("💾 Saved transaction: {} - {}", saved.getId(), saved.getReference());
        return saved;
    }

    @Transactional
    public List<Transaction> saveAllTransactions(List<Transaction> transactions) {
        log.info("📋 Saving {} transactions", transactions.size());

        // Auto-categorize batch
        List<Transaction> categorized = categoryService.processBatch(transactions);

        List<Transaction> saved = transactionRepository.saveAll(categorized);
        log.info("✅ Saved {} transactions successfully", saved.size());
        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<Transaction> findById(Long id) {
        return transactionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Transaction> findAll(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
        log.info("🗑️ Deleted transaction: {}", id);
    }

    /**
     * 🔍 Query Operations
     */
    @Transactional(readOnly = true)
    public List<Transaction> findByBankName(String bankName) {
        return transactionRepository.findByBankName(bankName);
    }

    @Transactional(readOnly = true)
    public List<Transaction> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByBookingDateBetween(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Transaction> findByCurrentMonth() {
        YearMonth currentMonth = YearMonth.now();
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();
        return findByDateRange(startOfMonth, endOfMonth);
    }

    @Transactional(readOnly = true)
    public List<Transaction> findByType(TransactionType type) {
        return transactionRepository.findByType(type);
    }

    @Transactional(readOnly = true)
    public List<Transaction> findExpenses() {
        return transactionRepository.findByTypeAndAmountLessThan(TransactionType.DEBIT, BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public List<Transaction> findIncome() {
        return transactionRepository.findByTypeAndAmountGreaterThan(TransactionType.CREDIT, BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public List<Transaction> findUncategorized() {
        return transactionRepository.findByCategoryIsNull();
    }

    @Transactional(readOnly = true)
    public List<Transaction> findByState(TransactionState state) {
        return transactionRepository.findByState(state);
    }

    @Transactional(readOnly = true)
    public List<Transaction> searchByReference(String keyword) {
        return transactionRepository.findByReferenceContainingIgnoreCase(keyword);
    }

    @Transactional(readOnly = true)
    public List<Transaction> findByCounterparty(String counterparty) {
        return transactionRepository.findByCounterpartyContainingIgnoreCase(counterparty);
    }

    /**
     * 📊 Financial Analytics
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalIncome(LocalDate startDate, LocalDate endDate) {
        BigDecimal total = transactionRepository.calculateTotalIncomeInPeriod(startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateTotalExpenses(LocalDate startDate, LocalDate endDate) {
        BigDecimal total = transactionRepository.calculateTotalExpensesInPeriod(startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateNetIncome(LocalDate startDate, LocalDate endDate) {
        BigDecimal income = calculateTotalIncome(startDate, endDate);
        BigDecimal expenses = calculateTotalExpenses(startDate, endDate);
        return income.subtract(expenses);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateCurrentMonthBalance() {
        YearMonth currentMonth = YearMonth.now();
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();
        return calculateNetIncome(startOfMonth, endOfMonth);
    }

    @Transactional(readOnly = true)
    public MonthlyReport generateMonthlyReport(YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Transaction> monthlyTransactions = findByDateRange(startDate, endDate);

        BigDecimal totalIncome = calculateTotalIncome(startDate, endDate);
        BigDecimal totalExpenses = calculateTotalExpenses(startDate, endDate);
        BigDecimal netIncome = totalIncome.subtract(totalExpenses);

        // Group by category
        Map<Category, BigDecimal> expensesByCategory = monthlyTransactions.stream()
                .filter(Transaction::isExpense)
                .filter(t -> t.getCategory() != null)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.mapping(
                                Transaction::getAbsoluteAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));

        return new MonthlyReport(
                yearMonth,
                monthlyTransactions.size(),
                totalIncome,
                totalExpenses,
                netIncome,
                expensesByCategory
        );
    }

    /**
     * 📈 Category Analytics
     */
    @Transactional(readOnly = true)
    public Map<Category, BigDecimal> getExpensesByCategory(LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = findByDateRange(startDate, endDate);

        return transactions.stream()
                .filter(Transaction::isExpense)
                .filter(t -> t.getCategory() != null)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.mapping(
                                Transaction::getAbsoluteAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));
    }

    @Transactional(readOnly = true)
    public List<CategorySummary> getCategorySummary(LocalDate startDate, LocalDate endDate) {
        Map<Category, BigDecimal> expensesByCategory = getExpensesByCategory(startDate, endDate);

        return expensesByCategory.entrySet().stream()
                .map(entry -> new CategorySummary(
                        entry.getKey(),
                        entry.getValue(),
                        countTransactionsByCategory(entry.getKey(), startDate, endDate)
                ))
                .sorted((a, b) -> b.totalAmount().compareTo(a.totalAmount()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countTransactionsByCategory(Category category, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByDateRange(startDate, endDate).stream()
                .filter(t -> category.equals(t.getCategory()))
                .count();
    }

    /**
     * 🏦 Banking Analytics
     */
    @Transactional(readOnly = true)
    public List<String> getAllBankNames() {
        return transactionRepository.findAllBankNames();
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getBankTransactionCounts() {
        return transactionRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        Transaction::getBankName,
                        Collectors.counting()
                ));
    }

    @Transactional(readOnly = true)
    public List<CounterpartyStats> getTopCounterparties(int limit) {
        return transactionRepository.findTopCounterpartiesByTransactionCount()
                .stream()
                .limit(limit)
                .map(result -> new CounterpartyStats(
                        (String) result[0],
                        ((Number) result[1]).longValue()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 🔧 Batch Operations
     */
    @Transactional
    public void recategorizeAll() {
        log.info("🔄 Starting re-categorization of all transactions");

        List<Transaction> uncategorized = findUncategorized();
        log.info("📋 Found {} uncategorized transactions", uncategorized.size());

        categoryService.processBatch(uncategorized);
        log.info("✅ Re-categorization completed");
    }

    @Transactional
    public void updateTransactionStates(List<Long> transactionIds, TransactionState newState) {
        List<Transaction> transactions = transactionRepository.findAllById(transactionIds);
        transactions.forEach(t -> t.setState(newState));
        transactionRepository.saveAll(transactions);

        log.info("🔄 Updated {} transactions to state: {}", transactions.size(), newState);
    }

    /**
     * 📊 Dashboard Statistics
     */
    @Transactional(readOnly = true)
    public DashboardStats getDashboardStats() {
        long totalTransactions = transactionRepository.count();
        long uncategorizedCount = transactionRepository.countByCategoryIsNull();

        YearMonth currentMonth = YearMonth.now();
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();

        BigDecimal monthlyIncome = calculateTotalIncome(startOfMonth, endOfMonth);
        BigDecimal monthlyExpenses = calculateTotalExpenses(startOfMonth, endOfMonth);
        BigDecimal monthlyBalance = monthlyIncome.subtract(monthlyExpenses);

        return new DashboardStats(
                totalTransactions,
                uncategorizedCount,
                monthlyIncome,
                monthlyExpenses,
                monthlyBalance,
                getAllBankNames().size()
        );
    }

    /**
     * 📊 DTOs for Analytics
     */
    public record MonthlyReport(
            YearMonth month,
            int transactionCount,
            BigDecimal totalIncome,
            BigDecimal totalExpenses,
            BigDecimal netIncome,
            Map<Category, BigDecimal> expensesByCategory
    ) {}

    public record CategorySummary(
            Category category,
            BigDecimal totalAmount,
            long transactionCount
    ) {}

    public record CounterpartyStats(
            String counterparty,
            long transactionCount
    ) {}

    public record DashboardStats(
            long totalTransactions,
            long uncategorizedTransactions,
            BigDecimal monthlyIncome,
            BigDecimal monthlyExpenses,
            BigDecimal monthlyBalance,
            int bankCount
    ) {}
}