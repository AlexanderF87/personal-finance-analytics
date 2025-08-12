package de.finance.analytics.transactions.service;

import de.finance.analytics.transactions.entity.Category;
import de.finance.analytics.transactions.entity.Transaction;
import de.finance.analytics.transactions.entity.TransactionType;
import de.finance.analytics.transactions.repository.CategoryRepository;
import de.finance.analytics.transactions.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CategorizationService with Mockito
 * Testing business logic without database
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategorizationService Tests")
class CategorizationServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategorizationService categorizationService;

    private List<Category> testCategories;

    @BeforeEach
    void setUp() {
        // Test Categories for auto-categorization
        Category groceries = new Category("groceries", "🍔 Lebensmittel", "🍔");
        groceries.setKeywords("REWE,EDEKA,Supermarkt,Lebensmittel,ALDI,LIDL");

        Category transport = new Category("transport", "🚗 Transport", "🚗");
        transport.setKeywords("Tankstelle,Shell,Aral,DB,Deutsche Bahn,MVG");

        Category housing = new Category("housing", "🏠 Wohnen", "🏠");
        housing.setKeywords("Miete,Stadtwerke,Strom,Gas,Wasser,GEZ");

        testCategories = Arrays.asList(groceries, transport, housing);
    }

    @Test
    @DisplayName("Should categorize REWE transaction as groceries")
    void shouldCategorizeReweTransactionAsGroceries() {
        // Given - REWE Transaction
        Transaction transaction = createTestTransaction("REWE Supermarkt Köln, Kartenzahlung");

        // Mock: CategoryRepository returns all categories
        when(categoryRepository.findAllByIsActiveTrue()).thenReturn(testCategories);

        // When - auto-categorization
        Optional<Category> result = categorizationService.categorizeTransaction(transaction);

        // Then - "groceries" categorization correct
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("groceries");
        assertThat(result.get().getIcon()).isEqualTo("🍔");

        // Verify: CategoryRepository was called
        verify(categoryRepository, times(1)).findAllByIsActiveTrue();
    }

    @Test
    @DisplayName("Should categorize Deutsche Bahn transaction as transport")
    void shouldCategorizeDeutscheBahnTransactionAsTransport() {
        // Given - Deutsche Bahn Transaction
        Transaction transaction = createTestTransaction("DB Vertrieb GmbH, Online-Ticket München-Berlin");

        // Mock Setup
        when(categoryRepository.findAllByIsActiveTrue()).thenReturn(testCategories);

        // When
        Optional<Category> result = categorizationService.categorizeTransaction(transaction);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("transport");
        assertThat(result.get().getIcon()).isEqualTo("🚗");
    }

    @Test
    @DisplayName("Should return empty for unknown transaction")
    void shouldReturnEmptyForUnknownTransaction() {
        // Given - unknown Transaction
        Transaction transaction = createTestTransaction("Unbekannter Zahlungsempfänger XYZ");

        // Mock Setup
        when(categoryRepository.findAllByIsActiveTrue()).thenReturn(testCategories);

        // When
        Optional<Category> result = categorizationService.categorizeTransaction(transaction);

        // Then - no categorization possible
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should process batch of transactions and save categorized ones")
    void shouldProcessBatchOfTransactionsAndSaveCategorizedOnes() {
        // Given - Batch of Transactions
        Transaction reweTransaction = createTestTransaction("REWE Supermarkt");
        Transaction shellTransaction = createTestTransaction("Shell Tankstelle");
        Transaction unknownTransaction = createTestTransaction("Unknown Vendor");

        List<Transaction> transactions = Arrays.asList(reweTransaction, shellTransaction, unknownTransaction);

        // Mock Setup
        when(categoryRepository.findAllByIsActiveTrue()).thenReturn(testCategories);
        when(transactionRepository.saveAll(any())).thenReturn(transactions);

        // When - Batch Processing
        List<Transaction> processedTransactions = categorizationService.processBatch(transactions);

        // Then - only categorized Transactions were processed
        assertThat(processedTransactions).hasSize(3);

        // Verify: REWE was categorized as groceries
        Transaction processedRewe = processedTransactions.stream()
                .filter(t -> t.getReference().contains("REWE"))
                .findFirst().orElseThrow();
        assertThat(processedRewe.getCategory()).isNotNull();
        assertThat(processedRewe.getCategory().getName()).isEqualTo("groceries");

        // Verify: Shell was categorized as transport
        Transaction processedShell = processedTransactions.stream()
                .filter(t -> t.getReference().contains("Shell"))
                .findFirst().orElseThrow();
        assertThat(processedShell.getCategory()).isNotNull();
        assertThat(processedShell.getCategory().getName()).isEqualTo("transport");

        // Verify: Unknown stays uncategorized
        Transaction processedUnknown = processedTransactions.stream()
                .filter(t -> t.getReference().contains("Unknown"))
                .findFirst().orElseThrow();
        assertThat(processedUnknown.getCategory()).isNull();

        // Verify: Repository Calls
        verify(categoryRepository, times(1)).findAllByIsActiveTrue();
        verify(transactionRepository, times(1)).saveAll(transactions);
    }

    @Test
    @DisplayName("Should handle case insensitive keyword matching")
    void shouldHandleCaseInsensitiveKeywordMatching() {
        // Given - different writings
        Transaction reweUpper = createTestTransaction("REWE SUPERMARKT");
        Transaction reweLower = createTestTransaction("rewe supermarkt");
        Transaction reweMixed = createTestTransaction("Rewe Supermarkt");

        // Mock Setup
        when(categoryRepository.findAllByIsActiveTrue()).thenReturn(testCategories);

        // When & Then - recognize all variants
        assertThat(categorizationService.categorizeTransaction(reweUpper)).isPresent();
        assertThat(categorizationService.categorizeTransaction(reweLower)).isPresent();
        assertThat(categorizationService.categorizeTransaction(reweMixed)).isPresent();

        // All categorized as groceries
        assertThat(categorizationService.categorizeTransaction(reweUpper))
                .isPresent()
                .map(Category::getName)
                .hasValue("groceries");

        assertThat(categorizationService.categorizeTransaction(reweLower))
                .isPresent()
                .map(Category::getName)
                .hasValue("groceries");

        assertThat(categorizationService.categorizeTransaction(reweMixed))
                .isPresent()
                .map(Category::getName)
                .hasValue("groceries");
    }

    // Helper Method
    private Transaction createTestTransaction(String reference) {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setBankName("Test Bank");
        transaction.setBookingDate(LocalDate.now());
        transaction.setType(TransactionType.DEBIT);
        transaction.setAmount(new BigDecimal("-50.00"));
        transaction.setReference(reference);
        transaction.setCounterparty("Test Counterparty");
        return transaction;
    }
}