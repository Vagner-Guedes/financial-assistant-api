package dio.budgeting.infrastructure.ai;

import dio.budgeting.application.CreateTransactionUseCase;
import dio.budgeting.application.GetSpendingSummaryUseCase;
import dio.budgeting.application.SearchTransactionsUseCase;
import dio.budgeting.application.input.CreateTransactionCommand;
import dio.budgeting.application.output.SpendingSummaryOutput;
import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.TransactionQuery;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class FinancialAssistantTools {
    private final CreateTransactionUseCase createTransactionUseCase;
    private final SearchTransactionsUseCase searchTransactionsUseCase;
    private final GetSpendingSummaryUseCase getSpendingSummaryUseCase;

    public FinancialAssistantTools(CreateTransactionUseCase createTransactionUseCase,
                                   SearchTransactionsUseCase searchTransactionsUseCase,
                                   GetSpendingSummaryUseCase getSpendingSummaryUseCase) {
        this.createTransactionUseCase = createTransactionUseCase;
        this.searchTransactionsUseCase = searchTransactionsUseCase;
        this.getSpendingSummaryUseCase = getSpendingSummaryUseCase;
    }

    @Tool(name = "create-transaction", description = "Registra um novo gasto financeiro")
    public TransactionOutput createTransaction(CreateTransactionToolInput input) {
        return createTransactionUseCase.execute(new CreateTransactionCommand(
                input.description(), input.amountCents(), input.category(), input.occurredAt()));
    }

    @Tool(name = "list-transactions", description = "Lista gastos por categoria e intervalo de datas")
    public List<TransactionOutput> listTransactions(TransactionSearchToolInput input) {
        return searchTransactionsUseCase.execute(new TransactionQuery(
                input.category(), input.from(), input.to()));
    }

    @Tool(name = "summarize-spending", description = "Calcula quantidade, total e distribuição dos gastos")
    public SpendingSummaryOutput summarizeSpending(TransactionSearchToolInput input) {
        return getSpendingSummaryUseCase.execute(new TransactionQuery(
                input.category(), input.from(), input.to()));
    }

    public record CreateTransactionToolInput(
            @ToolParam(description = "Descrição curta do gasto") String description,
            @ToolParam(description = "Valor em centavos de real; por exemplo, R$ 39,90 vira 3990") long amountCents,
            @ToolParam(description = "Categoria: FOOD, HEALTH, TRANSPORT, HOUSING, LEISURE ou OTHER") Category category,
            @ToolParam(description = "Data no formato yyyy-MM-dd; use a data atual quando o áudio não informar outra") LocalDate occurredAt) {
    }

    public record TransactionSearchToolInput(
            @ToolParam(description = "Categoria opcional: FOOD, HEALTH, TRANSPORT, HOUSING, LEISURE ou OTHER") Category category,
            @ToolParam(description = "Data inicial opcional no formato yyyy-MM-dd") LocalDate from,
            @ToolParam(description = "Data final opcional no formato yyyy-MM-dd") LocalDate to) {
    }
}
