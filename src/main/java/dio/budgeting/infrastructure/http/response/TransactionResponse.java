package dio.budgeting.infrastructure.http.response;

import dio.budgeting.application.output.TransactionOutput;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(String id, String description, String category,
                                  String categoryLabel, BigDecimal amount,
                                  LocalDate occurredAt) {
    public static TransactionResponse from(TransactionOutput output) {
        return new TransactionResponse(output.id(), output.description(), output.category().code(),
                output.category().label(), output.amount(), output.occurredAt());
    }
}
