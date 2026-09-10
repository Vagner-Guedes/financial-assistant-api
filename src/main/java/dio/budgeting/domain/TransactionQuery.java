package dio.budgeting.domain;

import java.time.LocalDate;

public record TransactionQuery(Category category, LocalDate from, LocalDate to) {
    public TransactionQuery {
        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidTransactionException("A data inicial não pode ser posterior à data final");
        }
    }
}
