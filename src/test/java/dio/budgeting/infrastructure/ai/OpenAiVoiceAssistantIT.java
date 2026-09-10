package dio.budgeting.infrastructure.ai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("openai")
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class OpenAiVoiceAssistantIT {
    @Autowired
    VoiceAssistant voiceAssistant;

    @Test
    void should_transcribeUseToolsAndGenerateAudio() {
        var result = voiceAssistant.process(new ClassPathResource("audio/recording-1.m4a"));

        assertThat(result.transcript()).containsIgnoringCase("80 reais");
        assertThat(result.text()).isNotBlank();
        assertThat(result.audio()).hasSizeGreaterThan(1024);
    }
}
