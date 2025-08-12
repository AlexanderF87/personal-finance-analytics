package de.finance.analytics.transactions.repository;

import de.finance.analytics.transactions.entity.Category;
import de.finance.analytics.transactions.entity.Transaction;
import de.finance.analytics.transactions.entity.TransactionState;
import de.finance.analytics.transactions.entity.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spring Boot Data JPA test for TransactionRepository
 * Using H2 In-Memory Database für quick tests
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TransactionRepository Tests")
class TransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    @DisplayName("Should save transaction with all banking fields")
    void shouldSaveTransactionWithAllBankingFields() {
        // Given - Banking transaction mit deutschen Daten
        Transaction transaction = new Transaction();
        transaction.setBankName("Sparkasse Köln/Bonn");
        transaction.setAccountNumber("DE89 3705 0198 0000 0123 45");
        transaction.setBookingDate(LocalDate.of(2024, 12, 15));
        transaction.setValueDate(LocalDate.of(2024, 12, 15));
        transaction.setType(TransactionType.DEBIT);
        transaction.setAmount(new BigDecimal("-89.95"));
        transaction.setCurrency("EUR");
        transaction.setReference("REWE Supermarkt Köln, Kartenzahlung");
        transaction.setCounterparty("REWE Group");
        transaction.setState(TransactionState.PENDING);
        transaction.setImportSource("CSV");
        transaction.setImportTimestamp(LocalDateTime.now());

        // When - save Transaction
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Then - saved all banking-fields correctly
        assertThat(savedTransaction.getId()).isNotNull();
        assertThat(savedTransaction.getBankName()).isEqualTo("Sparkasse Köln/Bonn");
        assertThat(savedTransaction.getAmount()).isEqualByComparingTo(new BigDecimal("-89.95"));
        assertThat(savedTransaction.getType()).isEqualTo(TransactionType.DEBIT);
        assertThat(savedTransaction.isExpense()).isTrue();
        assertThat(savedTransaction.getAbsoluteAmount()).isEqualByComparingTo(new BigDecimal("89.95"));
    }

    @Test
    @DisplayName("Should find transactions by bank name")
    void shouldFindTransactionsByBankName() {
        // Given - Transactions of different banks
        Transaction sparkasseTransaction = createTestTransaction("Sparkasse", new BigDecimal("-50.00"));
        Transaction dkbTransaction = createTestTransaction("DKB", new BigDecimal("-75.00"));
        Transaction ingTransaction = createTestTransaction("ING", new BigDecimal("1500.00"));

        entityManager.persistAndFlush(sparkasseTransaction);
        entityManager.persistAndFlush(dkbTransaction);
        entityManager.persistAndFlush(ingTransaction);

        // When - search for Sparkasse-Transactions
        List<Transaction> sparkasseTransactions = transactionRepository.findByBankName("Sparkasse");

        // Then - only find Sparkasse-Transactions
        assertThat(sparkasseTransactions).hasSize(1);
        assertThat(sparkasseTransactions.get(0).getBankName()).isEqualTo("Sparkasse");
        assertThat(sparkasseTransactions.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("-50.00"));
    }

    @Test
    @DisplayName("Should find transactions by date range")
    void shouldFindTransactionsByDateRange() {
        // Given - Transactions over different months
        Transaction decemberTransaction = createTestTransactionWithDate(LocalDate.of(2024, 12, 15));
        Transaction januaryTransaction = createTestTransactionWithDate(LocalDate.of(2025, 1, 10));
        Transaction februaryTransaction = createTestTransactionWithDate(LocalDate.of(2025, 2, 5));

        entityManager.persistAndFlush(decemberTransaction);
        entityManager.persistAndFlush(januaryTransaction);
        entityManager.persistAndFlush(februaryTransaction);

        // When - search for january-Transactions
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);
        List<Transaction> januaryTransactions = transactionRepository.findByBookingDateBetween(startDate, endDate);

        // Then - found january-Transaction only
        assertThat(januaryTransactions).hasSize(1);
        assertThat(januaryTransactions.getFirst().getBookingDate().getMonth().getValue()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should find expenses vs income correctly")
    void shouldFindExpensesVsIncomeCorrectly() {
        // Given - mix of credit and debit
        Transaction expense1 = createTestTransaction("Sparkasse", new BigDecimal("-100.00"), TransactionType.DEBIT);
        Transaction expense2 = createTestTransaction("DKB", new BigDecimal("-50.00"), TransactionType.DEBIT);
        Transaction income = createTestTransaction("ING", new BigDecimal("2500.00"), TransactionType.CREDIT);

        entityManager.persistAndFlush(expense1);
        entityManager.persistAndFlush(expense2);
        entityManager.persistAndFlush(income);

        // When - search for credit and debit
        List<Transaction> expenses = transactionRepository.findByTypeAndAmountLessThan(
                TransactionType.DEBIT, BigDecimal.ZERO);
        List<Transaction> incomes = transactionRepository.findByTypeAndAmountGreaterThan(
                TransactionType.CREDIT, BigDecimal.ZERO);

        // Then - correct differentiation of credit and debit
        assertThat(expenses).hasSize(2);
        assertThat(incomes).hasSize(1);
        assertThat(expenses.get(0).isExpense()).isTrue();
        assertThat(incomes.get(0).isIncome()).isTrue();
    }

    @Test
    @DisplayName("Should handle transaction with category relationship")
    void shouldHandleTransactionWithCategoryRelationship() {
        // Given - create Category
        Category groceryCategory = new Category();
        groceryCategory.setName("groceries");
        groceryCategory.setDisplayName("🍔 Lebensmittel");
        groceryCategory.setIcon("🍔");
        groceryCategory.setKeywords("REWE,EDEKA,Supermarkt");
        Category savedCategory = entityManager.persistAndFlush(groceryCategory);

        // Transaction with Category
        Transaction transaction = createTestTransaction("Sparkasse", new BigDecimal("-89.95"));
        transaction.setReference("REWE Supermarkt Köln");
        transaction.setCategory(savedCategory);

        // When - save Transaction with Category
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Then - save Category-relationship correctly
        Optional<Transaction> foundTransaction = transactionRepository.findById(savedTransaction.getId());
        assertThat(foundTransaction).isPresent();
        assertThat(foundTransaction.get().getCategory()).isNotNull();
        assertThat(foundTransaction.get().getCategory().getName()).isEqualTo("groceries");
        assertThat(foundTransaction.get().getCategory().getIcon()).isEqualTo("🍔");
    }

    // Helper methods for Test-Data
    private Transaction createTestTransaction(String bankName, BigDecimal amount) {
        return createTestTransaction(bankName, amount,
                amount.compareTo(BigDecimal.ZERO) < 0 ? TransactionType.DEBIT : TransactionType.CREDIT);
    }

    private Transaction createTestTransaction(String bankName, BigDecimal amount, TransactionType type) {
        Transaction transaction = new Transaction();
        transaction.setBankName(bankName);
        transaction.setBookingDate(LocalDate.now());
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setReference("Test Transaction");
        transaction.setCounterparty("Test Counterparty");
        transaction.setState(TransactionState.PENDING);
        transaction.setImportTimestamp(LocalDateTime.now());
        return transaction;
    }

    private Transaction createTestTransactionWithDate(LocalDate date) {
        Transaction transaction = createTestTransaction("Test Bank", new BigDecimal("-100.00"));
        transaction.setBookingDate(date);
        return transaction;
    }
}