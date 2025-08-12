package de.finance.analytics.transactions.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Entity tests for Transaction business logic
 * Testing banking-specific methods and validations
 */
@DisplayName("Transaction Entity Tests")
class TransactionEntityTest {

    @Test
    @DisplayName("Should identify expense transactions correctly")
    void shouldIdentifyExpenseTransactionsCorrectly() {
        // Given - Negative DEBIT Transaction (deutsche Banking-Konvention)
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

        // When - Set to banking precision
        BigDecimal bankingAmount = transaction.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP);
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
}