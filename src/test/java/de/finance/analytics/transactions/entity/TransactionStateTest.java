package de.finance.analytics.transactions.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Entity tests for TransactionState Enum
 * Testing banking-specific state logic - VERVOLLSTÄNDIGT für 100% Coverage
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

        // Failure states are not processable (except FAILED which is retryable)
        for (TransactionState state : failureStates) {
            if (state != TransactionState.FAILED) { // FAILED is processable (retry)
                assertThat(state.isProcessable()).isFalse();
            }
        }
    }

    // 🆕 NEUE TESTS für 100% Coverage
    @ParameterizedTest
    @EnumSource(TransactionState.class)
    @DisplayName("Should test all enum values comprehensively")
    void shouldTestAllEnumValuesComprehensively(TransactionState state) {
        // When & Then - All states have required properties
        assertThat(state.getDisplayName()).isNotEmpty();
        assertThat(state.getDescription()).isNotEmpty();
        assertThat(state.name()).isNotEmpty();

        // State logic is consistent
        if (state.isComplete()) {
            assertThat(state.isProcessable()).isFalse(); // Complete states shouldn't be processable
        }
    }

    @Test
    @DisplayName("Should handle all state combinations correctly")
    void shouldHandleAllStateCombinationsCorrectly() {
        // Given - All states
        TransactionState[] allStates = TransactionState.values();

        // When & Then - Verify all states
        assertThat(allStates).hasSize(4);
        assertThat(allStates).contains(
                TransactionState.PENDING,
                TransactionState.PROCESSED,
                TransactionState.FAILED,
                TransactionState.CANCELLED
        );
    }

    @Test
    @DisplayName("Should handle enum constructor correctly")
    void shouldHandleEnumConstructorCorrectly() {
        // When - Create enum instances (constructor coverage)
        TransactionState pending = TransactionState.valueOf("PENDING");
        TransactionState processed = TransactionState.valueOf("PROCESSED");

        // Then - Constructor worked correctly
        assertThat(pending).isEqualTo(TransactionState.PENDING);
        assertThat(processed).isEqualTo(TransactionState.PROCESSED);

        assertThat(pending.getDisplayName()).isEqualTo("Importiert");
        assertThat(processed.getDisplayName()).isEqualTo("Verarbeitet");
    }

    @Test
    @DisplayName("Should handle processing workflow states")
    void shouldHandleProcessingWorkflowStates() {
        // Given - Typical workflow: PENDING -> PROCESSED
        TransactionState initialState = TransactionState.PENDING;
        TransactionState finalState = TransactionState.PROCESSED;

        // When & Then - Workflow logic
        assertThat(initialState.isProcessable()).isTrue();
        assertThat(initialState.isComplete()).isFalse();

        assertThat(finalState.isProcessable()).isFalse();
        assertThat(finalState.isComplete()).isTrue();
    }

    @Test
    @DisplayName("Should handle error workflow states")
    void shouldHandleErrorWorkflowStates() {
        // Given - Error workflow: PENDING -> FAILED (retryable) or CANCELLED (final)
        TransactionState failedState = TransactionState.FAILED;
        TransactionState cancelledState = TransactionState.CANCELLED;

        // When & Then - Error handling logic
        assertThat(failedState.isProcessable()).isTrue(); // Can retry
        assertThat(failedState.isComplete()).isFalse();

        assertThat(cancelledState.isProcessable()).isFalse(); // Final state
        assertThat(cancelledState.isComplete()).isFalse(); // Not successfully complete
    }
}