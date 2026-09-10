package dio.budgeting.application.output;

import dio.budgeting.domain.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public record TransactionOutput(String id, String description, CategoryOutput category,
                                BigDecimal amount, LocalDate occurredAt) {

    public static TransactionOutput from(Transaction transaction) {
        return new TransactionOutput(
                transaction.id().value().toString(),
                transaction.description(),
                new CategoryOutput(transaction.category().name(), transaction.category().getLabel()),
                BigDecimal.valueOf(transaction.amountCents(), 2).setScale(2, RoundingMode.UNNECESSARY),
                transaction.occurredAt()
        );
    }

    public record CategoryOutput(String code, String label) {
    }
}
