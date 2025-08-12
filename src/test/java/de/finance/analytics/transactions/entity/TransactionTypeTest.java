package de.finance.analytics.transactions.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Entity tests for TransactionType Enum
 * Testing banking-specific transaction-types
 */
@DisplayName("TransactionType Enum Tests")
class TransactionTypeTest {

    @Test
    @DisplayName("Should have correct German banking names")
    void shouldHaveCorrectGermanBankingNames() {
        // When & Then - German banking terminology
        assertThat(TransactionType.DEBIT.getGermanName()).isEqualTo("Belastung");
        assertThat(TransactionType.CREDIT.getGermanName()).isEqualTo("Gutschrift");
    }

    @Test
    @DisplayName("Should have correct German display names")
    void shouldHaveCorrectGermanDisplayNames() {
        // When & Then - German UI names
        assertThat(TransactionType.DEBIT.getDisplayName()).isEqualTo("Ausgabe");
        assertThat(TransactionType.CREDIT.getDisplayName()).isEqualTo("Einnahme");
    }

    @Test
    @DisplayName("Should identify expense types correctly")
    void shouldIdentifyExpenseTypesCorrectly() {
        // When & Then - Expense identification
        assertThat(TransactionType.DEBIT.isExpense()).isTrue();
        assertThat(TransactionType.CREDIT.isExpense()).isFalse();
    }

    @Test
    @DisplayName("Should identify income types correctly")
    void shouldIdentifyIncomeTypesCorrectly() {
        // When & Then - Income identification
        assertThat(TransactionType.CREDIT.isIncome()).isTrue();
        assertThat(TransactionType.DEBIT.isIncome()).isFalse();
    }

    @Test
    @DisplayName("Should handle German banking conventions")
    void shouldHandleGermanBankingConventions() {
        // Given - German banking uses these terms
        // DEBIT = Belastung (money leaves account) = Ausgabe
        // CREDIT = Gutschrift (money comes to account) = Einnahme

        // When & Then - Conventions followed
        assertThat(TransactionType.DEBIT.getGermanName()).contains("Belastung");
        assertThat(TransactionType.CREDIT.getGermanName()).contains("Gutschrift");

        // Expense/Income logic matches German conventions
        assertThat(TransactionType.DEBIT.isExpense()).isTrue(); // Belastung = Ausgabe
        assertThat(TransactionType.CREDIT.isIncome()).isTrue(); // Gutschrift = Einnahme
    }

    @Test
    @DisplayName("Should be mutually exclusive")
    void shouldBeMutuallyExclusive() {
        // When & Then - Types are mutually exclusive
        assertThat(TransactionType.DEBIT.isExpense()).isTrue();
        assertThat(TransactionType.DEBIT.isIncome()).isFalse();

        assertThat(TransactionType.CREDIT.isIncome()).isTrue();
        assertThat(TransactionType.CREDIT.isExpense()).isFalse();
    }

    @Test
    @DisplayName("Should cover all transaction scenarios")
    void shouldCoverAllTransactionScenarios() {
        // Given - All possible transaction types in German banking
        TransactionType[] allTypes = TransactionType.values();

        // When & Then - Complete coverage
        assertThat(allTypes).hasSize(2); // Only DEBIT and CREDIT needed
        assertThat(allTypes).contains(TransactionType.DEBIT, TransactionType.CREDIT);

        // Every type is either expense or income
        for (TransactionType type : allTypes) {
            assertThat(type.isExpense() ^ type.isIncome()).isTrue(); // XOR: exactly one is true
        }
    }
}