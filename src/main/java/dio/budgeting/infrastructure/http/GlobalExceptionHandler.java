package dio.budgeting.infrastructure.http;

import dio.budgeting.domain.InvalidTransactionException;
import dio.budgeting.infrastructure.ai.AiNotConfiguredException;
import dio.budgeting.infrastructure.http.response.ApiError;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        var details = new LinkedHashMap<String, String>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> details.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return response(HttpStatus.BAD_REQUEST, "Dados inválidos", "Revise os campos enviados", details);
    }

    @ExceptionHandler({InvalidTransactionException.class, ConstraintViolationException.class,
            IllegalArgumentException.class, HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class})
    ResponseEntity<ApiError> handleBadRequest(Exception exception) {
        return response(HttpStatus.BAD_REQUEST, "Requisição inválida", exception.getMessage(), Map.of());
    }

    @ExceptionHandler(AiNotConfiguredException.class)
    ResponseEntity<ApiError> handleAiUnavailable(AiNotConfiguredException exception) {
        return response(HttpStatus.SERVICE_UNAVAILABLE, "IA indisponível", exception.getMessage(), Map.of());
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String error, String message,
                                              Map<String, String> details) {
        return ResponseEntity.status(status).body(
                new ApiError(Instant.now(), status.value(), error, message, details));
    }
}
