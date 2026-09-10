package dio.budgeting.infrastructure.config;

import dio.budgeting.application.GetSpendingSummaryUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ApplicationConfig {

    @Bean
    Clock applicationClock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    GetSpendingSummaryUseCase getSpendingSummaryUseCase(
            dio.budgeting.domain.TransactionRepository transactionRepository) {
        return new GetSpendingSummaryUseCase(transactionRepository);
    }
}
