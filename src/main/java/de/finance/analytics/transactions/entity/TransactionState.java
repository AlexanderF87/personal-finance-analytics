package de.finance.analytics.transactions.entity;

import lombok.Getter;

/**
 * Transaction processing states for banking pipeline
 * German display names for UI, English enum values for code
 */
@Getter
public enum TransactionState {
    /**
     * PENDING = Import successful, waiting for categorization
     */
    PENDING("Importiert", "Wartet auf Verarbeitung"),

    /**
     * PROCESSED = Categorization completed, ready for analytics
     */
    PROCESSED("Verarbeitet", "Kategorisiert und analysiert"),

    /**
     * FAILED = Import or processing failed
     */
    FAILED("Fehlgeschlagen", "Fehler beim Import/Verarbeitung"),

    /**
     * CANCELLED = Transaction cancelled or invalid
     */
    CANCELLED("Storniert", "Transaction ungültig");

    private final String displayName; // German for UI
    private final String description; // German for UI

    TransactionState(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public boolean isProcessable() {
        return this == PENDING || this == FAILED;
    }

    public boolean isComplete() {
        return this == PROCESSED;
    }
}