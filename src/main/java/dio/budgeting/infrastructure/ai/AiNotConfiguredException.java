package dio.budgeting.infrastructure.ai;

public class AiNotConfiguredException extends RuntimeException {
    public AiNotConfiguredException(String message) {
        super(message);
    }

    public AiNotConfiguredException(String message, Throwable cause) {
        super(message, cause);
    }
}
