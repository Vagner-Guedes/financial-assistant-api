package dio.budgeting.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionTest {

    @Test
    void should_createTransaction_when_dataIsValid() {
        var transaction = new Transaction("  Mercado  ", 3990, Category.FOOD, LocalDate.now());

        assertThat(transaction.description()).isEqualTo("Mercado");
        assertThat(transaction.amountCents()).isEqualTo(3990);
        assertThat(transaction.category()).isEqualTo(Category.FOOD);
    }

    @Test
    void should_rejectTransaction_when_amountIsOutOfRange() {
        assertThatThrownBy(() -> new Transaction("Mercado", 0, Category.FOOD, LocalDate.now()))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("valor");
    }

    @Test
    void should_rejectTransaction_when_dateIsInTheFuture() {
        assertThatThrownBy(() -> new Transaction("Mercado", 3990, Category.FOOD, LocalDate.now().plusDays(1)))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("futuro");
    }

    @Test
    void should_rejectQuery_when_periodIsInverted() {
        assertThatThrownBy(() -> new TransactionQuery(Category.FOOD,
                LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 1)))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("inicial");
    }
}
