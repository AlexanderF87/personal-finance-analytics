package de.finance.analytics.transactions.service;

import de.finance.analytics.transactions.entity.Category;
import de.finance.analytics.transactions.entity.Transaction;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CategorizationService {

    public Optional<Category> categorizeTransaction(Transaction transaction) {
        return Optional.empty();
    }

    public List<Transaction> processBatch(List<Transaction> transactions) {
        return Collections.emptyList();
    }
}
