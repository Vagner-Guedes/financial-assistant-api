package dio.budgeting.infrastructure.http.request;

import dio.budgeting.application.input.CreateTransactionCommand;
import dio.budgeting.domain.Category;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public record CreateTransactionRequest(
        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 120, message = "A descrição deve ter no máximo 120 caracteres")
        String description,

        @NotNull(message = "O valor é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
        @DecimalMax(value = "100000.00", message = "O valor máximo é R$ 100.000,00")
        @Digits(integer = 6, fraction = 2, message = "O valor deve ter no máximo duas casas decimais")
        BigDecimal amount,

        @NotNull(message = "A categoria é obrigatória")
        Category category,

        LocalDate occurredAt
) {
    public CreateTransactionCommand toCommand() {
        var amountCents = amount.setScale(2, RoundingMode.UNNECESSARY)
                .movePointRight(2)
                .longValueExact();
        return new CreateTransactionCommand(description, amountCents, category, occurredAt);
    }
}
