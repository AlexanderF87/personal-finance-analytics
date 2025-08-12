package de.finance.analytics.transactions.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Entity tests for TransactionState Enum
 * Testing banking-specific state logic
 */
@DisplayName("TransactionState Enum Tests")
class TransactionStateTest {

    @Test
    @DisplayName("Should have correct German display names")
    void shouldHaveCorrectGermanDisplayNames() {
        // When & Then - German UI names
        assertThat(TransactionState.PENDING.getDisplayName()).isEqualTo("Importiert");
        assertThat(TransactionState.PROCESSED.getDisplayName()).isEqualTo("Verarbeitet");
        assertThat(TransactionState.FAILED.getDisplayName()).isEqualTo("Fehlgeschlagen");
        assertThat(TransactionState.CANCELLED.getDisplayName()).isEqualTo("Storniert");
    }

    @Test
    @DisplayName("Should have correct German descriptions")
    void shouldHaveCorrectGermanDescriptions() {
        // When & Then - German descriptions
        assertThat(TransactionState.PENDING.getDescription()).isEqualTo("Wartet auf Verarbeitung");
        assertThat(TransactionState.PROCESSED.getDescription()).isEqualTo("Kategorisiert und analysiert");
        assertThat(TransactionState.FAILED.getDescription()).isEqualTo("Fehler beim Import/Verarbeitung");
        assertThat(TransactionState.CANCELLED.getDescription()).isEqualTo("Transaction ungültig");
    }

    @Test
    @DisplayName("Should identify processable states correctly")
    void shouldIdentifyProcessableStatesCorrectly() {
        // When & Then - Processable states
        assertThat(TransactionState.PENDING.isProcessable()).isTrue();
        assertThat(TransactionState.FAILED.isProcessable()).isTrue();
        assertThat(TransactionState.PROCESSED.isProcessable()).isFalse();
        assertThat(TransactionState.CANCELLED.isProcessable()).isFalse();
    }

    @Test
    @DisplayName("Should identify complete states correctly")
    void shouldIdentifyCompleteStatesCorrectly() {
        // When & Then - Complete states
        assertThat(TransactionState.PROCESSED.isComplete()).isTrue();
        assertThat(TransactionState.PENDING.isComplete()).isFalse();
        assertThat(TransactionState.FAILED.isComplete()).isFalse();
        assertThat(TransactionState.CANCELLED.isComplete()).isFalse();
    }

    @Test
    @DisplayName("Should handle state transitions correctly")
    void shouldHandleStateTransitionsCorrectly() {
        // Given - Banking processing pipeline states
        TransactionState[] processingOrder = {
                TransactionState.PENDING,
                TransactionState.PROCESSED
        };

        TransactionState[] failureStates = {
                TransactionState.FAILED,
                TransactionState.CANCELLED
        };

        // When & Then - Processing pipeline
        for (TransactionState state : processingOrder) {
            assertThat(state.isProcessable() || state.isComplete()).isTrue();
        }

        // Failure states are not processable
        for (TransactionState state : failureStates) {
            if (state != TransactionState.FAILED) { // FAILED is processable (retry)
                assertThat(state.isProcessable()).isFalse();
            }
        }
    }
}