package dio.budgeting.domain;

import java.time.LocalDate;

public final class Transaction {
    public static final long MAX_AMOUNT_CENTS = 10_000_000L;
    public static final int MAX_DESCRIPTION_LENGTH = 120;

    private final TransactionId id;
    private final String description;
    private final long amountCents;
    private final Category category;
    private final LocalDate occurredAt;

    public Transaction(TransactionId id, String description, long amountCents,
                       Category category, LocalDate occurredAt) {
        this.id = id;
        this.description = normalizeDescription(description);
        if (amountCents <= 0 || amountCents > MAX_AMOUNT_CENTS) {
            throw new InvalidTransactionException("O valor deve estar entre R$ 0,01 e R$ 100.000,00");
        }
        if (category == null) {
            throw new InvalidTransactionException("A categoria da transação é obrigatória");
        }
        if (occurredAt == null) {
            throw new InvalidTransactionException("A data da transação é obrigatória");
        }
        if (occurredAt.isAfter(LocalDate.now())) {
            throw new InvalidTransactionException("A data da transação não pode estar no futuro");
        }
        this.amountCents = amountCents;
        this.category = category;
        this.occurredAt = occurredAt;
    }

    public Transaction(String description, long amountCents, Category category, LocalDate occurredAt) {
        this(new TransactionId(), description, amountCents, category, occurredAt);
    }

    private static String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new InvalidTransactionException("A descrição da transação é obrigatória");
        }
        var normalized = description.trim();
        if (normalized.length() > MAX_DESCRIPTION_LENGTH) {
            throw new InvalidTransactionException("A descrição deve ter no máximo 120 caracteres");
        }
        return normalized;
    }

    public TransactionId id() {
        return id;
    }

    public String description() {
        return description;
    }

    public long amountCents() {
        return amountCents;
    }

    public Category category() {
        return category;
    }

    public LocalDate occurredAt() {
        return occurredAt;
    }
}
