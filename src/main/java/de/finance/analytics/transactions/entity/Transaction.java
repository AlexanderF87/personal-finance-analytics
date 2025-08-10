package de.finance.analytics.transactions.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Banking Transaction Entity for German banking data
 * Supports multi-format import: CSV, PDF, MT940, CAMT.053
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Banking identifiers
    @Column(name = "bank_name", nullable = false)
    private String bankName; // "Sparkasse", "DKB", "ING", etc.

    @Column(name = "account_number")
    private String accountNumber; // IBAN or account number

    // Banking dates
    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate; // Transaction date

    @Column(name = "value_date")
    private LocalDate valueDate; // Value date

    // Transaction core data
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType type; // DEBIT, CREDIT

    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal amount; // Amount in EUR (banking-precise)

    @Column(name = "currency", length = 3)
    private String currency = "EUR"; // ISO 4217 currency code

    // Banking reference data
    @Column(name = "reference", length = 500)
    private String reference; // Purpose/description

    @Column(name = "counterparty", length = 200)
    private String counterparty; // Sender/recipient

    // Categorization (future: machine learning)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category; // Auto-categorized

    // Processing state
    @Enumerated(EnumType.STRING)
    @Column(name = "processing_state", nullable = false)
    private TransactionState state = TransactionState.PENDING;

    // Import metadata
    @Column(name = "import_source")
    private String importSource; // "CSV", "PDF", "MT940"

    @Column(name = "import_timestamp")
    private LocalDateTime importTimestamp;

    @Column(name = "raw_data", columnDefinition = "TEXT")
    private String rawData; // Original CSV/PDF line for debugging

    // Banking-specific constructor
    public Transaction(String bankName, LocalDate bookingDate, TransactionType type,
                       BigDecimal amount, String reference, String counterparty) {
        this.bankName = bankName;
        this.bookingDate = bookingDate;
        this.type = type;
        this.amount = amount;
        this.reference = reference;
        this.counterparty = counterparty;
        this.importTimestamp = LocalDateTime.now();
        // state = PENDING is already set in field
        // currency = "EUR" is already set in field
    }

    // Banking helper methods
    public boolean isExpense() {
        return type == TransactionType.DEBIT && amount.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isIncome() {
        return type == TransactionType.CREDIT && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal getAbsoluteAmount() {
        return amount.abs();
    }

    // toString for debugging (no @ToString due to JPA relations!)
    @Override
    public String toString() {
        return String.format("Transaction{id=%d, bank='%s', date=%s, amount=%s, reference='%s'}",
                id, bankName, bookingDate, amount, reference);
    }
}