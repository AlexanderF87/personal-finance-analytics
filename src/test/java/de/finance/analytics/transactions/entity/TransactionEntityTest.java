package de.finance.analytics.transactions.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Entity tests for Transaction business logic
 * Testing banking-specific methods and validations
 */
@DisplayName("Transaction Entity Tests")
class TransactionEntityTest {

    // ========== YOUR EXISTING TESTS ==========

    @Test
    @DisplayName("Should identify expense transactions correctly")
    void shouldIdentifyExpenseTransactionsCorrectly() {
        // Given - Negative DEBIT Transaction (German banking convention)
        Transaction expenseTransaction = new Transaction(
                "Sparkasse Köln/Bonn",
                LocalDate.now(),
                TransactionType.DEBIT,
                new BigDecimal("-89.95"),
                "REWE Supermarkt Köln",
                "REWE Group"
        );

        // When & Then - Expense works correctly
        assertThat(expenseTransaction.isExpense()).isTrue();
        assertThat(expenseTransaction.isIncome()).isFalse();
        assertThat(expenseTransaction.getType()).isEqualTo(TransactionType.DEBIT);
        assertThat(expenseTransaction.getAmount()).isLessThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should identify income transactions correctly")
    void shouldIdentifyIncomeTransactionsCorrectly() {
        // Given - Positive CREDIT Transaction
        Transaction incomeTransaction = new Transaction(
                "DKB Deutsche Kreditbank",
                LocalDate.now(),
                TransactionType.CREDIT,
                new BigDecimal("3500.00"),
                "Gehalt Dezember 2024",
                "ABC Software GmbH"
        );

        // When & Then - Income works correctly
        assertThat(incomeTransaction.isIncome()).isTrue();
        assertThat(incomeTransaction.isExpense()).isFalse();
        assertThat(incomeTransaction.getType()).isEqualTo(TransactionType.CREDIT);
        assertThat(incomeTransaction.getAmount()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should calculate absolute amount correctly for expenses")
    void shouldCalculateAbsoluteAmountCorrectlyForExpenses() {
        // Given - Negative expense amount
        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal("-125.50"));
        transaction.setType(TransactionType.DEBIT);

        // When - Absolute amount calculation
        BigDecimal absoluteAmount = transaction.getAbsoluteAmount();

        // Then - Positive value returned
        assertThat(absoluteAmount).isEqualByComparingTo(new BigDecimal("125.50"));
        assertThat(absoluteAmount).isPositive();

        // Original amount unchanged
        assertThat(transaction.getAmount()).isEqualByComparingTo(new BigDecimal("-125.50"));
    }

    @Test
    @DisplayName("Should calculate absolute amount correctly for income")
    void shouldCalculateAbsoluteAmountCorrectlyForIncome() {
        // Given - Positive income amount
        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal("2500.00"));
        transaction.setType(TransactionType.CREDIT);

        // When - Absolute amount calculation
        BigDecimal absoluteAmount = transaction.getAbsoluteAmount();

        // Then - Same positive value returned
        assertThat(absoluteAmount).isEqualByComparingTo(new BigDecimal("2500.00"));
        assertThat(absoluteAmount).isPositive();
    }

    @Test
    @DisplayName("Should handle edge case: zero amount")
    void shouldHandleEdgeCaseZeroAmount() {
        // Given - Zero amount transaction
        Transaction zeroTransaction = new Transaction();
        zeroTransaction.setAmount(BigDecimal.ZERO);
        zeroTransaction.setType(TransactionType.DEBIT);

        // When & Then - Edge case handling
        assertThat(zeroTransaction.isExpense()).isFalse();
        assertThat(zeroTransaction.isIncome()).isFalse();
        assertThat(zeroTransaction.getAbsoluteAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should handle banking precision correctly")
    void shouldHandleBankingPrecisionCorrectly() {
        // Given - Banking-typical precision (2 decimal places)
        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal("123.456789")); // More precision than banking

        // When - Set to banking precision (FIXED: Deprecated API)
        BigDecimal bankingAmount = transaction.getAmount().setScale(2, RoundingMode.HALF_UP);
        transaction.setAmount(bankingAmount);

        // Then - Correct banking precision
        assertThat(transaction.getAmount()).isEqualByComparingTo(new BigDecimal("123.46"));
        assertThat(transaction.getAmount().scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should create transaction with constructor correctly")
    void shouldCreateTransactionWithConstructorCorrectly() {
        // Given - Constructor parameters
        String bankName = "ING Deutschland";
        LocalDate bookingDate = LocalDate.of(2024, 12, 15);
        TransactionType type = TransactionType.DEBIT;
        BigDecimal amount = new BigDecimal("-45.80");
        String reference = "Shell Tankstelle München";
        String counterparty = "Shell Deutschland";

        // When - Create transaction
        Transaction transaction = new Transaction(bankName, bookingDate, type, amount, reference, counterparty);

        // Then - All fields set correctly
        assertThat(transaction.getBankName()).isEqualTo(bankName);
        assertThat(transaction.getBookingDate()).isEqualTo(bookingDate);
        assertThat(transaction.getType()).isEqualTo(type);
        assertThat(transaction.getAmount()).isEqualByComparingTo(amount);
        assertThat(transaction.getReference()).isEqualTo(reference);
        assertThat(transaction.getCounterparty()).isEqualTo(counterparty);

        // Default values set
        assertThat(transaction.getImportTimestamp()).isNotNull();
        assertThat(transaction.getCurrency()).isEqualTo("EUR"); // Default currency
    }

    @Test
    @DisplayName("Should handle toString without errors")
    void shouldHandleToStringWithoutErrors() {
        // Given - Transaction with all fields
        Transaction transaction = new Transaction(
                "Commerzbank AG",
                LocalDate.of(2024, 12, 10),
                TransactionType.CREDIT,
                new BigDecimal("1000.00"),
                "Bonus Dezember",
                "Employer XYZ"
        );
        transaction.setId(42L);

        // When - toString called
        String result = transaction.toString();

        // Then - Contains important information
        assertThat(result).contains("Transaction");
        assertThat(result).contains("id=42");
        assertThat(result).contains("Commerzbank AG");
        assertThat(result).contains("2024-12-10");
        assertThat(result).contains("1000.00");
        assertThat(result).contains("Bonus Dezember");
    }

    @Test
    @DisplayName("Should set default state to PENDING")
    void shouldSetDefaultStateToPending() {
        // Given - New transaction
        Transaction transaction = new Transaction();

        // When - Check default state
        TransactionState defaultState = transaction.getState();

        // Then - Default is PENDING
        assertThat(defaultState).isEqualTo(TransactionState.PENDING);
    }

    // ========== MISSING TESTS (that I'm adding) ==========

    @Nested
    @DisplayName("Edge Cases & Boundary Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle positive DEBIT transaction as non-expense")
        void shouldHandlePositiveDebitAsNonExpense() {
            // WHY: Sometimes positive DEBIT exists (e.g. reversals, chargebacks)
            Transaction transaction = new Transaction();
            transaction.setType(TransactionType.DEBIT);
            transaction.setAmount(new BigDecimal("50.00")); // Positive!

            assertThat(transaction.isExpense()).isFalse(); // Not expense due to positive amount
            assertThat(transaction.isIncome()).isFalse();  // Not income due to DEBIT type
        }

        @Test
        @DisplayName("Should handle negative CREDIT transaction as non-income")
        void shouldHandleNegativeCreditAsNonIncome() {
            // WHY: Sometimes negative CREDIT exists (e.g. fee reversals)
            Transaction transaction = new Transaction();
            transaction.setType(TransactionType.CREDIT);
            transaction.setAmount(new BigDecimal("-25.00")); // Negative!

            assertThat(transaction.isIncome()).isFalse();   // Not income due to negative amount
            assertThat(transaction.isExpense()).isFalse();  // Not expense due to CREDIT type
        }

        @Test
        @DisplayName("Should handle very large amounts")
        void shouldHandleVeryLargeAmounts() {
            // WHY: Banking must handle large amounts (millions)
            Transaction transaction = new Transaction();
            transaction.setType(TransactionType.CREDIT);
            transaction.setAmount(new BigDecimal("999999999.99")); // ~1 billion

            assertThat(transaction.isIncome()).isTrue();
            assertThat(transaction.getAbsoluteAmount()).isEqualByComparingTo(new BigDecimal("999999999.99"));
        }

        @Test
        @DisplayName("Should handle very small amounts")
        void shouldHandleVerySmallAmounts() {
            // WHY: Micro-transactions, rounding differences
            Transaction transaction = new Transaction();
            transaction.setType(TransactionType.DEBIT);
            transaction.setAmount(new BigDecimal("-0.01")); // 1 cent

            assertThat(transaction.isExpense()).isTrue();
            assertThat(transaction.getAbsoluteAmount()).isEqualByComparingTo(new BigDecimal("0.01"));
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create transaction with default constructor")
        void shouldCreateTransactionWithDefaultConstructor() {
            // When - Create with default constructor
            Transaction transaction = new Transaction();

            // Then - Default values are set
            assertThat(transaction.getId()).isNull();
            assertThat(transaction.getCurrency()).isEqualTo("EUR");
            assertThat(transaction.getState()).isEqualTo(TransactionState.PENDING);
        }

        @Test
        @DisplayName("Should create transaction with all-args constructor")
        void shouldCreateTransactionWithAllArgsConstructor() {
            // Given - All constructor parameters
            Long id = 123L;
            String bankName = "Deutsche Bank";
            String accountNumber = "DE89370400440532013000";
            LocalDate bookingDate = LocalDate.of(2024, 1, 15);
            LocalDate valueDate = LocalDate.of(2024, 1, 16);
            TransactionType type = TransactionType.CREDIT;
            BigDecimal amount = new BigDecimal("1500.00");
            String currency = "EUR";
            String reference = "Salary January";
            String counterparty = "Tech Corp GmbH";
            Category category = null; // Would need Category entity
            TransactionState state = TransactionState.PROCESSED;
            String importSource = "CSV";
            LocalDateTime importTimestamp = LocalDateTime.now();
            String rawData = "2024-01-15,CREDIT,1500.00,Salary January";

            // When - Create with all args constructor
            Transaction transaction = new Transaction(
                    id, bankName, accountNumber, bookingDate, valueDate,
                    type, amount, currency, reference, counterparty,
                    category, state, importSource, importTimestamp, rawData
            );

            // Then - All fields are set correctly
            assertThat(transaction.getId()).isEqualTo(id);
            assertThat(transaction.getBankName()).isEqualTo(bankName);
            assertThat(transaction.getAccountNumber()).isEqualTo(accountNumber);
            assertThat(transaction.getBookingDate()).isEqualTo(bookingDate);
            assertThat(transaction.getValueDate()).isEqualTo(valueDate);
            assertThat(transaction.getType()).isEqualTo(type);
            assertThat(transaction.getAmount()).isEqualByComparingTo(amount);
            assertThat(transaction.getCurrency()).isEqualTo(currency);
            assertThat(transaction.getReference()).isEqualTo(reference);
            assertThat(transaction.getCounterparty()).isEqualTo(counterparty);
            assertThat(transaction.getCategory()).isEqualTo(category);
            assertThat(transaction.getState()).isEqualTo(state);
            assertThat(transaction.getImportSource()).isEqualTo(importSource);
            assertThat(transaction.getImportTimestamp()).isEqualTo(importTimestamp);
            assertThat(transaction.getRawData()).isEqualTo(rawData);
        }

        @Test
        @DisplayName("Should set import timestamp automatically in custom constructor")
        void shouldSetImportTimestampAutomaticallyInCustomConstructor() {
            // Given - Time before creation
            LocalDateTime before = LocalDateTime.now();
            try {
                    Thread.sleep(1);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // When - Create transaction with custom constructor
            Transaction transaction = new Transaction(
                    "Volksbank",
                    LocalDate.now(),
                    TransactionType.DEBIT,
                    new BigDecimal("-50.00"),
                    "Grocery shopping",
                    "ALDI SÜD"
            );

            // Then - Import timestamp is set automatically and recent
            assertThat(transaction.getImportTimestamp()).isNotNull();
            assertThat(transaction.getImportTimestamp()).isAfter(before);
            assertThat(transaction.getImportTimestamp()).isCloseTo(
                    LocalDateTime.now(),
                    within(5, ChronoUnit.SECONDS)
            );
        }
    }

    @Nested
    @DisplayName("Field Validation Tests")
    class FieldValidationTests {

        @Test
        @DisplayName("Should handle null and empty strings gracefully")
        void shouldHandleNullAndEmptyStringsGracefully() {
            // Given - Transaction with null/empty strings
            Transaction transaction = new Transaction();
            transaction.setBankName(null);
            transaction.setReference("");
            transaction.setCounterparty("   "); // Whitespace only

            // When & Then - No exceptions thrown
            assertThat(transaction.getBankName()).isNull();
            assertThat(transaction.getReference()).isEmpty();
            assertThat(transaction.getCounterparty()).isEqualTo("   ");

            // toString should handle nulls gracefully
            String result = transaction.toString();
            assertThat(result).contains("Transaction");
        }

        @Test
        @DisplayName("Should handle different date combinations")
        void shouldHandleDifferentDateCombinations() {
            // Given - Transaction with different booking and value dates
            Transaction transaction = new Transaction();
            LocalDate bookingDate = LocalDate.of(2024, 1, 15);
            LocalDate valueDate = LocalDate.of(2024, 1, 17); // Weekend processing

            transaction.setBookingDate(bookingDate);
            transaction.setValueDate(valueDate);

            // When & Then - Both dates are stored correctly
            assertThat(transaction.getBookingDate()).isEqualTo(bookingDate);
            assertThat(transaction.getValueDate()).isEqualTo(valueDate);
            assertThat(transaction.getValueDate()).isAfter(transaction.getBookingDate());
        }

        @Test
        @DisplayName("Should handle different currencies")
        void shouldHandleDifferentCurrencies() {
            // Given - Transaction with non-EUR currency
            Transaction transaction = new Transaction();
            transaction.setCurrency("USD");
            transaction.setAmount(new BigDecimal("100.00"));
            transaction.setType(TransactionType.CREDIT);

            // When & Then - Currency is stored and income calculation works
            assertThat(transaction.getCurrency()).isEqualTo("USD");
            assertThat(transaction.isIncome()).isTrue(); // Should work regardless of currency
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should classify complex banking scenarios correctly")
        void shouldClassifyComplexBankingScenariosCorrectly() {
            // Scenario 1: Refund (positive DEBIT)
            Transaction refund = createTransaction(TransactionType.DEBIT, "15.99");
            assertThat(refund.isExpense()).isFalse();
            assertThat(refund.isIncome()).isFalse();

            // Scenario 2: Fee reversal (negative CREDIT)
            Transaction feeReversal = createTransaction(TransactionType.CREDIT, "-5.00");
            assertThat(feeReversal.isIncome()).isFalse();
            assertThat(feeReversal.isExpense()).isFalse();

            // Scenario 3: Normal expense
            Transaction expense = createTransaction(TransactionType.DEBIT, "-99.95");
            assertThat(expense.isExpense()).isTrue();
            assertThat(expense.isIncome()).isFalse();

            // Scenario 4: Normal income
            Transaction income = createTransaction(TransactionType.CREDIT, "2500.00");
            assertThat(income.isIncome()).isTrue();
            assertThat(income.isExpense()).isFalse();
        }

        @Test
        @DisplayName("Should maintain consistency between amount and type")
        void shouldMaintainConsistencyBetweenAmountAndType() {
            // WHY: Business rule - DEBIT usually negative, CREDIT usually positive
            // But exceptions exist, so we test both

            Transaction transaction = new Transaction();

            // Test typical DEBIT (negative)
            transaction.setType(TransactionType.DEBIT);
            transaction.setAmount(new BigDecimal("-50.00"));
            assertThat(transaction.isExpense()).isTrue();

            // Test atypical DEBIT (positive) - should not be expense
            transaction.setAmount(new BigDecimal("50.00"));
            assertThat(transaction.isExpense()).isFalse();

            // Test typical CREDIT (positive)
            transaction.setType(TransactionType.CREDIT);
            transaction.setAmount(new BigDecimal("100.00"));
            assertThat(transaction.isIncome()).isTrue();

            // Test atypical CREDIT (negative) - should not be income
            transaction.setAmount(new BigDecimal("-100.00"));
            assertThat(transaction.isIncome()).isFalse();
        }

        private Transaction createTransaction(TransactionType type, String amount) {
            Transaction transaction = new Transaction();
            transaction.setType(type);
            transaction.setAmount(new BigDecimal(amount));
            return transaction;
        }
    }

    @Nested
    @DisplayName("State Management Tests")
    class StateManagementTests {

        @Test
        @DisplayName("Should handle all transaction states correctly")
        void shouldHandleAllTransactionStatesCorrectly() {
            Transaction transaction = new Transaction();

            // Test default state
            assertThat(transaction.getState()).isEqualTo(TransactionState.PENDING);

            // Test state transitions (assuming these states exist)
            for (TransactionState state : TransactionState.values()) {
                transaction.setState(state);
                assertThat(transaction.getState()).isEqualTo(state);
            }
        }

        @Test
        @DisplayName("Should handle import metadata correctly")
        void shouldHandleImportMetadataCorrectly() {
            // Given - Transaction with import metadata
            Transaction transaction = new Transaction();
            String importSource = "MT940";
            LocalDateTime importTime = LocalDateTime.of(2024, 1, 15, 10, 30);
            String rawData = "123456789,CREDIT,1000.00,Salary";

            // When - Set import metadata
            transaction.setImportSource(importSource);
            transaction.setImportTimestamp(importTime);
            transaction.setRawData(rawData);

            // Then - Metadata is stored correctly
            assertThat(transaction.getImportSource()).isEqualTo(importSource);
            assertThat(transaction.getImportTimestamp()).isEqualTo(importTime);
            assertThat(transaction.getRawData()).isEqualTo(rawData);
        }
    }
}