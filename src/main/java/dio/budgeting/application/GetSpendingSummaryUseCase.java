package dio.budgeting.application;

import dio.budgeting.application.output.SpendingSummaryOutput;
import dio.budgeting.domain.TransactionQuery;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.stereotype.Service;

public class GetSpendingSummaryUseCase {
    private final TransactionRepository transactionRepository;

    public GetSpendingSummaryUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public SpendingSummaryOutput execute(TransactionQuery query) {
        return SpendingSummaryOutput.from(query.from(), query.to(), query.category(),
                transactionRepository.findAll(query));
    }
}
