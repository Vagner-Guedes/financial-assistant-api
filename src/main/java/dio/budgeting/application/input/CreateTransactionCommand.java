package dio.budgeting.application.input;

import dio.budgeting.domain.Category;

import java.time.LocalDate;

public record CreateTransactionCommand(String description, long amountCents,
                                       Category category, LocalDate occurredAt) {
}
