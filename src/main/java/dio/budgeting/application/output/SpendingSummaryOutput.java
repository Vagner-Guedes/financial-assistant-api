package dio.budgeting.application.output;

import dio.budgeting.domain.Category;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;

public record SpendingSummaryOutput(LocalDate from, LocalDate to, Category category,
                                    long transactionCount, BigDecimal totalAmount,
                                    Map<Category, BigDecimal> totalsByCategory) {

    public static SpendingSummaryOutput from(LocalDate from, LocalDate to, Category category,
                                              java.util.List<dio.budgeting.domain.Transaction> transactions) {
        var breakdown = transactions.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        dio.budgeting.domain.Transaction::category,
                        java.util.stream.Collectors.reducing(
                                0L,
                                dio.budgeting.domain.Transaction::amountCents,
                                Long::sum)))
                .entrySet()
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> toAmount(entry.getValue())));

        var totalCents = transactions.stream()
                .mapToLong(dio.budgeting.domain.Transaction::amountCents)
                .sum();

        return new SpendingSummaryOutput(from, to, category, transactions.size(),
                toAmount(totalCents), breakdown);
    }

    private static BigDecimal toAmount(long cents) {
        return BigDecimal.valueOf(cents, 2).setScale(2, RoundingMode.UNNECESSARY);
    }
}
