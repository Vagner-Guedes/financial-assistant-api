package dio.budgeting.application;

import dio.budgeting.application.input.CreateTransactionCommand;
import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
public class CreateTransactionUseCase {
    private final TransactionRepository transactionRepository;
    private final Clock clock;

    public CreateTransactionUseCase(TransactionRepository transactionRepository, Clock clock) {
        this.transactionRepository = transactionRepository;
        this.clock = clock;
    }

    public TransactionOutput execute(CreateTransactionCommand command) {
        var occurredAt = command.occurredAt() == null
                ? java.time.LocalDate.now(clock)
                : command.occurredAt();
        var transaction = new Transaction(command.description(), command.amountCents(),
                command.category(), occurredAt);
        return TransactionOutput.from(transactionRepository.save(transaction));
    }
}
