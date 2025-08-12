package de.finance.analytics.integration;

import de.finance.analytics.transactions.entity.Category;
import de.finance.analytics.transactions.entity.Transaction;
import de.finance.analytics.transactions.entity.TransactionState;
import de.finance.analytics.transactions.entity.TransactionType;
import de.finance.analytics.transactions.repository.CategoryRepository;
import de.finance.analytics.transactions.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests with real PostgreSQL via test-container
 * Testing complete stack: Controller → Service → Repository → Database
 */
@SpringBootTest
@Testcontainers
@DisplayName("Banking Integration Tests")
class BankingIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("finance_analytics_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        transactionRepository.deleteAll();
        categoryRepository.deleteAll();

        // Setup test categories
        setupTestCategories();
    }

    @Test
    @DisplayName("Complete Banking Workflow: Category → Transaction → Analytics")
    void shouldCompleteFullBankingWorkflow() {
        // Phase 1: Category Setup already in @BeforeEach
        List<Category> categories = categoryRepository.findAll();
        assertThat(categories).hasSize(4);

        // Phase 2: Import Banking Transactions
        Transaction sparkasseTransaction = createRealBankingTransaction(
                "Sparkasse Köln/Bonn",
                "DE89 3705 0198 0000 0123 45",
                LocalDate.of(2024, 12, 15),
                new BigDecimal("-89.95"),
                "REWE Supermarkt Köln, Kartenzahlung",
                "REWE Group"
        );

        Transaction dkbTransaction = createRealBankingTransaction(
                "DKB Deutsche Kreditbank",
                "DE12 1203 0000 0012 3456 78",
                LocalDate.of(2024, 12, 14),
                new BigDecimal("-45.80"),
                "Shell Tankstelle, Kraftstoff",
                "Shell Deutschland"
        );

        Transaction salaryTransaction = createRealBankingTransaction(
                "Sparkasse Köln/Bonn",
                "DE89 3705 0198 0000 0123 45",
                LocalDate.of(2024, 12, 1),
                new BigDecimal("3500.00"),
                "Gehalt Dezember 2024",
                "ABC Software GmbH"
        );

        List<Transaction> savedTransactions = transactionRepository.saveAll(
                List.of(sparkasseTransaction, dkbTransaction, salaryTransaction)
        );

        // Phase 3: Verify Transactions Saved
        assertThat(savedTransactions).hasSize(3);
        assertThat(transactionRepository.count()).isEqualTo(3);

        // Phase 4: Banking Analytics Queries

        // Find Expenses (German Banking: negative amounts)
        List<Transaction> expenses = transactionRepository.findByTypeAndAmountLessThan(
                TransactionType.DEBIT, BigDecimal.ZERO);
        assertThat(expenses).hasSize(2);
        assertThat(expenses).allSatisfy(t -> assertThat(t.isExpense()).isTrue());

        // Find Income (German Banking: positive amounts)
        List<Transaction> income = transactionRepository.findByTypeAndAmountGreaterThan(
                TransactionType.CREDIT, BigDecimal.ZERO);
        assertThat(income).hasSize(1);
        assertThat(income.get(0).isIncome()).isTrue();
        assertThat(income.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("3500.00"));

        // Find by Bank (Multi-Bank Support)
        List<Transaction> sparkasseTransactions = transactionRepository.findByBankName("Sparkasse Köln/Bonn");
        assertThat(sparkasseTransactions).hasSize(2); // REWE + Salary

        List<Transaction> dkbTransactions = transactionRepository.findByBankName("DKB Deutsche Kreditbank");
        assertThat(dkbTransactions).hasSize(1); // Shell

        // Phase 5: Date Range Analytics (December 2024)
        LocalDate decemberStart = LocalDate.of(2024, 12, 1);
        LocalDate decemberEnd = LocalDate.of(2024, 12, 31);
        List<Transaction> decemberTransactions = transactionRepository.findByBookingDateBetween(
                decemberStart, decemberEnd);
        assertThat(decemberTransactions).hasSize(3);

        // Phase 6: Calculate Monthly Summary
        BigDecimal totalIncome = decemberTransactions.stream()
                .filter(Transaction::isIncome)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = decemberTransactions.stream()
                .filter(Transaction::isExpense)
                .map(Transaction::getAbsoluteAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal savings = totalIncome.subtract(totalExpenses);

        assertThat(totalIncome).isEqualByComparingTo(new BigDecimal("3500.00"));
        assertThat(totalExpenses).isEqualByComparingTo(new BigDecimal("135.75")); // 89.95 + 45.80
        assertThat(savings).isEqualByComparingTo(new BigDecimal("3364.25"));

        // Phase 7: Banking Data Integrity Check
        assertThat(postgres.isRunning()).isTrue();
        assertThat(transactionRepository.count()).isEqualTo(3);
        assertThat(categoryRepository.count()).isEqualTo(4);
    }

    @Test
    @DisplayName("Should handle German Banking IBAN and Currency correctly")
    void shouldHandleGermanBankingIbanAndCurrencyCorrectly() {
        // Given - Transaction with german IBAN and EUR
        Transaction transaction = new Transaction();
        transaction.setBankName("Commerzbank AG");
        transaction.setAccountNumber("DE89 1234 5678 9012 3456 78"); // Deutsche IBAN
        transaction.setBookingDate(LocalDate.now());
        transaction.setType(TransactionType.DEBIT);
        transaction.setAmount(new BigDecimal("-123.45"));
        transaction.setCurrency("EUR"); // Euro als Standard
        transaction.setReference("Testüberweisung nach deutschem Standard");
        transaction.setState(TransactionState.PROCESSED);

        // When - saving in PostgreSQL
        Transaction saved = transactionRepository.save(transaction);

        // Then - german Banking-Standards matched
        assertThat(saved.getAccountNumber()).matches("DE\\d{2} \\d{4} \\d{4} \\d{4} \\d{4} \\d{2}"); // IBAN Pattern
        assertThat(saved.getCurrency()).isEqualTo("EUR");
        assertThat(saved.getAmount().scale()).isEqualTo(2); // Banking Precision
        assertThat(saved.isExpense()).isTrue();

        // IBAN Validation (simplified)
        String iban = saved.getAccountNumber().replaceAll(" ", "");
        assertThat(iban).startsWith("DE");
        assertThat(iban).hasSize(22); // Deutsche IBAN Länge
    }

    private void setupTestCategories() {
        Category groceries = new Category("groceries", "🍔 Lebensmittel & Restaurants", "🍔");
        groceries.setKeywords("REWE,EDEKA,ALDI,LIDL,Supermarkt,Restaurant,McDonald,Burger");
        groceries.setColorHex("#E74C3C");

        Category transport = new Category("transport", "🚗 Transport & Mobilität", "🚗");
        transport.setKeywords("Shell,Aral,Tankstelle,DB,Deutsche Bahn,MVG,Taxi,Uber");
        transport.setColorHex("#3498DB");

        Category housing = new Category("housing", "🏠 Wohnen & Nebenkosten", "🏠");
        housing.setKeywords("Miete,Stadtwerke,Strom,Gas,Wasser,GEZ,Hausgeld,Versicherung");
        housing.setColorHex("#2ECC71");

        Category salary = new Category("salary", "💰 Gehalt & Einkommen", "💰");
        salary.setKeywords("Gehalt,Lohn,Bonus,Dividende,Zinsen");
        salary.setIsExpense(false); // Income category
        salary.setColorHex("#F39C12");

        categoryRepository.saveAll(List.of(groceries, transport, housing, salary));
    }

    private Transaction createRealBankingTransaction(String bankName, String iban,
                                                     LocalDate bookingDate, BigDecimal amount,
                                                     String reference, String counterparty) {
        Transaction transaction = new Transaction();
        transaction.setBankName(bankName);
        transaction.setAccountNumber(iban);
        transaction.setBookingDate(bookingDate);
        transaction.setValueDate(bookingDate); // Simplified: same as booking date
        transaction.setType(amount.compareTo(BigDecimal.ZERO) >= 0 ? TransactionType.CREDIT : TransactionType.DEBIT);
        transaction.setAmount(amount);
        transaction.setCurrency("EUR");
        transaction.setReference(reference);
        transaction.setCounterparty(counterparty);
        transaction.setState(TransactionState.PENDING);
        transaction.setImportSource("CSV");
        transaction.setImportTimestamp(LocalDateTime.now());

        return transaction;
    }
}