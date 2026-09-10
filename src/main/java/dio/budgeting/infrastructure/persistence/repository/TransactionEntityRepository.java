package dio.budgeting.infrastructure.persistence.repository;

import dio.budgeting.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface TransactionEntityRepository extends JpaRepository<TransactionEntity, UUID>,
        JpaSpecificationExecutor<TransactionEntity> {
}
