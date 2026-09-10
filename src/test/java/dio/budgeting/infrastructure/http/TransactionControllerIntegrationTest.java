package dio.budgeting.infrastructure.http;

import dio.budgeting.domain.Category;
import dio.budgeting.infrastructure.persistence.repository.TransactionEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionControllerIntegrationTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    TransactionEntityRepository transactionEntityRepository;

    @BeforeEach
    void cleanDatabase() {
        transactionEntityRepository.deleteAll();
    }

    @Test
    void should_createTransactionAndReturnAmountInReais() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Mercado",
                                  "amount": 39.90,
                                  "category": "FOOD",
                                  "occurredAt": "2026-09-09"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description", is("Mercado")))
                .andExpect(jsonPath("$.amount", is(39.90)))
                .andExpect(jsonPath("$.category", is("FOOD")))
                .andExpect(jsonPath("$.categoryLabel", is("Alimentação")));
    }

    @Test
    void should_filterTransactionsByCategoryAndPeriod() throws Exception {
        var today = LocalDate.now();
        createTransaction("Mercado", "50.00", "FOOD", today.minusDays(2));
        createTransaction("Farmácia", "30.00", "HEALTH", today.minusDays(1));
        createTransaction("Restaurante", "20.00", "FOOD", today);

        mockMvc.perform(get("/api/transactions")
                        .param("category", "FOOD")
                        .param("from", today.minusDays(2).toString())
                        .param("to", today.minusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].description", is("Mercado")));
    }

    @Test
    void should_returnSpendingSummaryWithBreakdown() throws Exception {
        var today = LocalDate.now();
        createTransaction("Mercado", "50.00", "FOOD", today);
        createTransaction("Ônibus", "30.00", "TRANSPORT", today);

        mockMvc.perform(get("/api/transactions/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionCount", is(2)))
                .andExpect(jsonPath("$.totalAmount", is(80.00)))
                .andExpect(jsonPath("$.totalsByCategory.FOOD", is(50.00)))
                .andExpect(jsonPath("$.totalsByCategory.TRANSPORT", is(30.00)));
    }

    @Test
    void should_returnBadRequest_whenTransactionIsInvalid() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "",
                                  "amount": -1,
                                  "category": null,
                                  "occurredAt": "2026-09-09"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.description").exists())
                .andExpect(jsonPath("$.details.amount").exists())
                .andExpect(jsonPath("$.details.category").exists());
    }

    @Test
    void should_returnServiceUnavailableForVoiceInLocalProfile() throws Exception {
        var file = new MockMultipartFile("file", "command.txt", "text/plain",
                "gastei 10 reais no mercado".getBytes());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart(
                                "/api/assistant/voice")
                        .file(file))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void should_exposeAssistantStatusForTheVisualPanel() throws Exception {
        mockMvc.perform(get("/api/assistant/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode", is("local")))
                .andExpect(jsonPath("$.available", is(false)));
    }

    private void createTransaction(String description, String amount, String category, LocalDate date)
            throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "%s",
                                  "amount": %s,
                                  "category": "%s",
                                  "occurredAt": "%s"
                                }
                                """.formatted(description, amount, category, date)))
                .andExpect(status().isCreated());
    }
}
