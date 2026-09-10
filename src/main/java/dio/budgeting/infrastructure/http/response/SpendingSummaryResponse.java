package dio.budgeting.infrastructure.http.response;

import dio.budgeting.application.output.SpendingSummaryOutput;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record SpendingSummaryResponse(LocalDate from, LocalDate to, String category,
                                      long transactionCount, BigDecimal totalAmount,
                                      Map<String, BigDecimal> totalsByCategory) {
    public static SpendingSummaryResponse from(SpendingSummaryOutput output) {
        var breakdown = output.totalsByCategory().entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(entry -> entry.getKey().name(), Map.Entry::getValue));
        return new SpendingSummaryResponse(
                output.from(), output.to(), output.category() == null ? null : output.category().name(),
                output.transactionCount(), output.totalAmount(), breakdown);
    }
}
