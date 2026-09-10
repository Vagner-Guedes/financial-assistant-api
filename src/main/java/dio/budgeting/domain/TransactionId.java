package dio.budgeting.domain;

import java.util.UUID;

public record TransactionId(UUID value) {

    public TransactionId {
        if (value == null) {
            throw new IllegalArgumentException("O identificador da transação é obrigatório");
        }
    }

    public TransactionId() {
        this(UUID.randomUUID());
    }
}
