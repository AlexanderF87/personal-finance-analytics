package de.finance.analytics.transactions.service;

import de.finance.analytics.transactions.entity.Category;
import de.finance.analytics.transactions.entity.Transaction;
import de.finance.analytics.transactions.entity.TransactionState;
import de.finance.analytics.transactions.repository.CategoryRepository;
import de.finance.analytics.transactions.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 🎯 CategoryService - Intelligent Banking Transaction Categorization
 *
 * Design Patterns:
 * - Strategy Pattern: Different categorization strategies
 * - Observer Pattern: Categorization event notifications
 * - Chain of Responsibility: Multiple categorization attempts
 *
 * Features:
 * - Keyword-based auto-categorization for German banks
 * - Machine learning preparation
 * - Category hierarchy support (Composite Pattern)
 * - Performance optimized with caching
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    // Cache for active categories (will be replaced with @Cacheable later)
    private List<Category> cachedActiveCategories;

    /**
     * 🔍 Auto-categorize single transaction using keyword matching
     */
    public Optional<Category> categorizeTransaction(Transaction transaction) {
        log.debug("🔍 Categorizing transaction: {}", transaction.getReference());

        if (transaction == null || transaction.getReference() == null) {
            return Optional.empty();
        }

        // Chain of Responsibility: Try different categorization strategies
        return categorizeByKeywords(transaction)
                .or(() -> categorizeByCounterparty(transaction))
                .or(() -> categorizeByAmount(transaction))
                .or(() -> getDefaultCategory());
    }

    /**
     * 📋 Batch process multiple transactions
     * Observer Pattern: Notify about categorization progress
     */
    public List<Transaction> processBatch(List<Transaction> transactions) {
        log.info("📋 Processing batch of {} transactions", transactions.size());

        int categorizedCount = 0;
        List<Category> activeCategories = getActiveCategories();

        for (Transaction transaction : transactions) {
            if (transaction.getCategory() == null) {
                Optional<Category> category = categorizeTransaction(transaction);
                if (category.isPresent()) {
                    transaction.setCategory(category.get());
                    transaction.setState(TransactionState.PROCESSED);
                    categorizedCount++;
                    log.debug("✅ Categorized: {} -> {}",
                            transaction.getReference(), category.get().getDisplayName());
                } else {
                    // Assign uncategorized category
                    getUncategorizedCategory().ifPresent(transaction::setCategory);
                    transaction.setState(TransactionState.PROCESSED);
                }
            }
        }

        // Save batch
        List<Transaction> savedTransactions = transactionRepository.saveAll(transactions);
        log.info("✅ Batch processed: {}/{} transactions categorized",
                categorizedCount, transactions.size());

        return savedTransactions;
    }

    /**
     * 🏷️ Keyword-based categorization (Primary strategy)
     */
    private Optional<Category> categorizeByKeywords(Transaction transaction) {
        String reference = normalizeText(transaction.getReference());
        String counterparty = normalizeText(transaction.getCounterparty());
        String searchText = (reference + " " + counterparty).toLowerCase().trim();

        return getActiveCategories().stream()
                .filter(category -> category.getKeywords() != null)
                .filter(category -> matchesKeywords(searchText, category.getKeywords()))
                .findFirst();
    }

    /**
     * 👥 Counterparty-based categorization (Secondary strategy)
     */
    private Optional<Category> categorizeByCounterparty(Transaction transaction) {
        if (transaction.getCounterparty() == null) return Optional.empty();

        String counterparty = normalizeText(transaction.getCounterparty());

        // Known German banks and financial institutions
        if (containsAny(counterparty, "sparkasse", "volksbank", "dkb", "ing", "commerzbank")) {
            return findCategoryByName("financial");
        }

        // Government and tax authorities
        if (containsAny(counterparty, "finanzamt", "stadt", "gemeinde", "bundesagentur")) {
            return findCategoryByName("government");
        }

        // Insurance companies
        if (containsAny(counterparty, "versicherung", "allianz", "axa", "generali")) {
            return findCategoryByName("insurance");
        }

        return Optional.empty();
    }

    /**
     * 💰 Amount-based categorization (Tertiary strategy)
     */
    private Optional<Category> categorizeByAmount(Transaction transaction) {
        // Large regular amounts might be salary
        if (transaction.isIncome() && transaction.getAmount().doubleValue() > 1500) {
            return findCategoryByName("salary");
        }

        // Small amounts might be transport/coffee
        if (transaction.isExpense() && transaction.getAbsoluteAmount().doubleValue() < 10) {
            return findCategoryByName("transport"); // Could be ÖPNV ticket
        }

        return Optional.empty();
    }

    /**
     * 🔧 Category Management Operations
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> getActiveCategories() {
        if (cachedActiveCategories == null) {
            cachedActiveCategories = categoryRepository.findAllByIsActiveTrue();
            log.debug("📦 Cached {} active categories", cachedActiveCategories.size());
        }
        return cachedActiveCategories;
    }

    public List<Category> getMainCategories() {
        return categoryRepository.findByParentCategoryIsNullAndIsActiveTrue();
    }

    public List<Category> getSubCategories(Category parentCategory) {
        return categoryRepository.findByParentCategoryAndIsActiveTrue(parentCategory);
    }

    public List<Category> getExpenseCategories() {
        return categoryRepository.findByIsExpenseAndIsActiveTrue(true);
    }

    public List<Category> getIncomeCategories() {
        return categoryRepository.findByIsExpenseAndIsActiveTrue(false);
    }

    /**
     * 💾 Category CRUD Operations
     */
    public Category saveCategory(Category category) {
        // Clear cache when categories are modified
        cachedActiveCategories = null;
        Category saved = categoryRepository.save(category);
        log.info("💾 Saved category: {}", saved.getDisplayName());
        return saved;
    }

    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    public Optional<Category> findByName(String name) {
        return categoryRepository.findByNameAndIsActiveTrue(name);
    }

    public void deleteCategory(Long id) {
        categoryRepository.findById(id).ifPresent(category -> {
            category.setIsActive(false); // Soft delete
            categoryRepository.save(category);
            cachedActiveCategories = null; // Clear cache
            log.info("🗑️ Deactivated category: {}", category.getDisplayName());
        });
    }

    /**
     * 📊 Analytics Support
     */
    public List<String> getCategoryColors() {
        return categoryRepository.findAllActiveColors();
    }

    public long countTransactionsByCategory(Category category) {
        return transactionRepository.countByCategory(category);
    }

    /**
     * 🔧 Helper Methods
     */
    private String normalizeText(String text) {
        if (text == null) return "";
        return text.toLowerCase()
                .replaceAll("ä", "ae")
                .replaceAll("ö", "oe")
                .replaceAll("ü", "ue")
                .replaceAll("ß", "ss")
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean matchesKeywords(String text, String keywords) {
        if (keywords == null || keywords.trim().isEmpty()) return false;

        String[] keywordArray = keywords.toLowerCase().split("[,;\\s]+");
        for (String keyword : keywordArray) {
            if (keyword.trim().length() > 2 && text.contains(keyword.trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }

    private Optional<Category> getDefaultCategory() {
        return getUncategorizedCategory();
    }

    private Optional<Category> getUncategorizedCategory() {
        return findCategoryByName("uncategorized");
    }

    private Optional<Category> findCategoryByName(String name) {
        return getActiveCategories().stream()
                .filter(cat -> name.equals(cat.getName()))
                .findFirst();
    }

    /**
     * 🧹 Cache Management
     */
    public void clearCache() {
        cachedActiveCategories = null;
        log.debug("🧹 Category cache cleared");
    }

    /**
     * 📈 Statistics for Dashboard
     */
    public CategoryStatistics getStatistics() {
        List<Category> active = getActiveCategories();
        long totalTransactions = transactionRepository.count();
        long uncategorizedCount = transactionRepository.countByCategoryIsNull();

        return new CategoryStatistics(
                active.size(),
                totalTransactions,
                uncategorizedCount,
                totalTransactions > 0 ? (double)(totalTransactions - uncategorizedCount) / totalTransactions * 100 : 0
        );
    }

    /**
     * 📊 Statistics DTO
     */
    public record CategoryStatistics(
            int totalCategories,
            long totalTransactions,
            long uncategorizedTransactions,
            double categorizationRate
    ) {}
}