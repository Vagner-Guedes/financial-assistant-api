package dio.budgeting.application;

import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.TransactionQuery;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchTransactionsUseCase {
    private final TransactionRepository transactionRepository;

    public SearchTransactionsUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<TransactionOutput> execute(TransactionQuery query) {
        return transactionRepository.findAll(query).stream()
                .map(TransactionOutput::from)
                .toList();
    }
}
