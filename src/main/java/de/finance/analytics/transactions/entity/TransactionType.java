package de.finance.analytics.transactions.entity;

import lombok.Getter;

/**
 * Banking transaction types according to German banking conventions
 * German display names for UI, English enum values for code
 */
@Getter
public enum TransactionType {
    /**
     * DEBIT = Debit/expense (money leaves the account)
     * Example: purchases, transfers to others
     */
    DEBIT("Belastung", "Ausgabe"),

    /**
     * CREDIT = Credit/income (money comes to account)
     * Example: salary, transfers from others
     */
    CREDIT("Gutschrift", "Einnahme");

    private final String germanName; // German banking term
    private final String displayName; // German for UI

    TransactionType(String germanName, String displayName) {
        this.germanName = germanName;
        this.displayName = displayName;
    }

    public boolean isExpense() {
        return this == DEBIT;
    }

    public boolean isIncome() {
        return this == CREDIT;
    }
}