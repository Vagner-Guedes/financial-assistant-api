package dio.budgeting.infrastructure.persistence.repository;

import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionQuery;
import dio.budgeting.domain.TransactionRepository;
import dio.budgeting.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JpaTransactionRepository implements TransactionRepository {
    private final TransactionEntityRepository transactionEntityRepository;

    public JpaTransactionRepository(TransactionEntityRepository transactionEntityRepository) {
        this.transactionEntityRepository = transactionEntityRepository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        return transactionEntityRepository.save(TransactionEntity.from(transaction)).toDomain();
    }

    @Override
    public List<Transaction> findAll(TransactionQuery query) {
        Specification<TransactionEntity> specification = Specification.unrestricted();

        if (query.category() != null) {
            specification = specification.and((root, ignored, builder) ->
                    builder.equal(root.get("category"), query.category()));
        }
        if (query.from() != null) {
            specification = specification.and((root, ignored, builder) ->
                    builder.greaterThanOrEqualTo(root.get("occurredAt"), query.from()));
        }
        if (query.to() != null) {
            specification = specification.and((root, ignored, builder) ->
                    builder.lessThanOrEqualTo(root.get("occurredAt"), query.to()));
        }

        return transactionEntityRepository.findAll(specification,
                        Sort.by(Sort.Direction.DESC, "occurredAt"))
                .stream()
                .map(TransactionEntity::toDomain)
                .toList();
    }
}
