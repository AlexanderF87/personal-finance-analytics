package de.finance.analytics.transactions.repository;

import de.finance.analytics.transactions.entity.Transaction;
import de.finance.analytics.transactions.entity.TransactionState;
import de.finance.analytics.transactions.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA Repository for banking Transactions
 * Supporting german banking-queries and analytics
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Banking-specific queries
    List<Transaction> findByBankName(String bankName);

    List<Transaction> findByAccountNumber(String accountNumber);

    // Date-based queries for analytics
    List<Transaction> findByBookingDateBetween(LocalDate startDate, LocalDate endDate);

    List<Transaction> findByBookingDateAfter(LocalDate date);

    // Amount-based queries (Expenses vs Income)
    List<Transaction> findByTypeAndAmountLessThan(TransactionType type, BigDecimal amount);

    List<Transaction> findByTypeAndAmountGreaterThan(TransactionType type, BigDecimal amount);

    // Categorization & Processing
    List<Transaction> findByState(TransactionState state);

    List<Transaction> findByCategoryIsNull(); // Unkategorisierte Transactions

    // Text-based-search for banking references
    List<Transaction> findByReferenceContainingIgnoreCase(String keyword);

    List<Transaction> findByCounterpartyContainingIgnoreCase(String counterparty);

    // Banking analytics queries
    @Query("SELECT t FROM Transaction t WHERE t.bookingDate BETWEEN :startDate AND :endDate " +
            "AND t.type = :type ORDER BY t.bookingDate DESC")
    List<Transaction> findTransactionsByDateRangeAndType(@Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate,
                                                         @Param("type") TransactionType type);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.bookingDate BETWEEN :startDate AND :endDate " +
            "AND t.type = 'CREDIT' AND t.amount > 0")
    BigDecimal calculateTotalIncomeInPeriod(@Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(ABS(t.amount)) FROM Transaction t WHERE t.bookingDate BETWEEN :startDate AND :endDate " +
            "AND t.type = 'DEBIT' AND t.amount < 0")
    BigDecimal calculateTotalExpensesInPeriod(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    // Multi-Bank support
    @Query("SELECT DISTINCT t.bankName FROM Transaction t ORDER BY t.bankName")
    List<String> findAllBankNames();

    // Import status tracking
    List<Transaction> findByImportSourceAndState(String importSource, TransactionState state);

    // Top counterparties (for analytics)
    @Query("SELECT t.counterparty, COUNT(t) as transactionCount FROM Transaction t " +
            "WHERE t.counterparty IS NOT NULL " +
            "GROUP BY t.counterparty " +
            "ORDER BY transactionCount DESC")
    List<Object[]> findTopCounterpartiesByTransactionCount();
}