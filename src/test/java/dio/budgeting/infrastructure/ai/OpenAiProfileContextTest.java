package dio.budgeting.infrastructure.ai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "spring.ai.openai.api-key=profile-context-test-key",
        "spring.datasource.url=jdbc:h2:mem:budgeting-openai-profile;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ActiveProfiles("openai")
class OpenAiProfileContextTest {

    @Test
    void openAiProfileLoadsWithoutCallingProvider() {
    }
}
