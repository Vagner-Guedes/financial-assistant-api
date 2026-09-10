package dio.budgeting.infrastructure.persistence.entity;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

import java.time.LocalDate;
import java.util.UUID;

@Entity
public class TransactionEntity {
    @Id
    private UUID id;
    private String description;
    private long amountCents;

    @Enumerated(EnumType.STRING)
    private Category category;
    private LocalDate occurredAt;

    protected TransactionEntity() {
    }

    private TransactionEntity(UUID id, String description, long amountCents,
                              Category category, LocalDate occurredAt) {
        this.id = id;
        this.description = description;
        this.amountCents = amountCents;
        this.category = category;
        this.occurredAt = occurredAt;
    }

    public static TransactionEntity from(Transaction transaction) {
        return new TransactionEntity(transaction.id().value(), transaction.description(),
                transaction.amountCents(), transaction.category(), transaction.occurredAt());
    }

    public Transaction toDomain() {
        return new Transaction(new TransactionId(id), description, amountCents, category, occurredAt);
    }

    public UUID getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public Category getCategory() {
        return category;
    }

    public LocalDate getOccurredAt() {
        return occurredAt;
    }
}
