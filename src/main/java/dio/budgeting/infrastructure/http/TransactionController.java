package dio.budgeting.infrastructure.http;

import dio.budgeting.application.CreateTransactionUseCase;
import dio.budgeting.application.GetSpendingSummaryUseCase;
import dio.budgeting.application.SearchTransactionsUseCase;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.TransactionQuery;
import dio.budgeting.infrastructure.http.request.CreateTransactionRequest;
import dio.budgeting.infrastructure.http.response.SpendingSummaryResponse;
import dio.budgeting.infrastructure.http.response.TransactionResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final CreateTransactionUseCase createTransactionUseCase;
    private final SearchTransactionsUseCase searchTransactionsUseCase;
    private final GetSpendingSummaryUseCase getSpendingSummaryUseCase;

    public TransactionController(CreateTransactionUseCase createTransactionUseCase,
                                 SearchTransactionsUseCase searchTransactionsUseCase,
                                 GetSpendingSummaryUseCase getSpendingSummaryUseCase) {
        this.createTransactionUseCase = createTransactionUseCase;
        this.searchTransactionsUseCase = searchTransactionsUseCase;
        this.getSpendingSummaryUseCase = getSpendingSummaryUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(@Valid @RequestBody CreateTransactionRequest request) {
        return TransactionResponse.from(createTransactionUseCase.execute(request.toCommand()));
    }

    @GetMapping
    public List<TransactionResponse> search(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return searchTransactionsUseCase.execute(new TransactionQuery(category, from, to)).stream()
                .map(TransactionResponse::from)
                .toList();
    }

    @GetMapping("/summary")
    public SpendingSummaryResponse summary(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return SpendingSummaryResponse.from(
                getSpendingSummaryUseCase.execute(new TransactionQuery(category, from, to)));
    }
}
