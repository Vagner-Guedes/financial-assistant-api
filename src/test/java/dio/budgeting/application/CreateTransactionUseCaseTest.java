package dio.budgeting.application;

import dio.budgeting.application.input.CreateTransactionCommand;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateTransactionUseCaseTest {

    @Test
    void should_useCurrentDate_whenDateIsOmitted() {
        var repository = mock(TransactionRepository.class);
        when(repository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var clock = Clock.fixed(Instant.parse("2026-09-10T12:00:00Z"), ZoneOffset.UTC);
        var useCase = new CreateTransactionUseCase(repository, clock);

        var output = useCase.execute(new CreateTransactionCommand("Café", 1250, Category.FOOD, null));

        assertThat(output.amount()).isEqualByComparingTo("12.50");
        assertThat(output.occurredAt()).isEqualTo(LocalDate.of(2026, 9, 10));
        assertThat(output.category().code()).isEqualTo("FOOD");
    }
}
