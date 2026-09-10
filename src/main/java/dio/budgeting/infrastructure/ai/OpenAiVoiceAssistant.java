package dio.budgeting.infrastructure.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@Profile("openai")
public class OpenAiVoiceAssistant implements VoiceAssistant {
    private static final Logger log = LoggerFactory.getLogger(OpenAiVoiceAssistant.class);

    private final OpenAiAudioTranscriptionModel transcriptionModel;
    private final ChatClient chatClient;
    private final OpenAiAudioSpeechModel textToSpeechModel;

    public OpenAiVoiceAssistant(OpenAiAudioTranscriptionModel transcriptionModel,
                                ChatClient.Builder chatClientBuilder,
                                @Value("classpath:prompts/system-message.st") Resource systemPrompt,
                                OpenAiAudioSpeechModel textToSpeechModel,
                                FinancialAssistantTools tools) throws IOException {
        this.transcriptionModel = transcriptionModel;
        this.chatClient = chatClientBuilder
                .defaultSystem(systemPrompt.getContentAsString(StandardCharsets.UTF_8))
                .defaultTools(tools)
                .build();
        this.textToSpeechModel = textToSpeechModel;
    }

    @Override
    public VoiceAssistantStatus status() {
        return new VoiceAssistantStatus("openai", true,
                "Assistente de voz ativo com transcrição, tools e síntese de áudio.");
    }

    @Override
    public VoiceAssistantResponse process(Resource audio) {
        try {
            var transcript = transcriptionModel.call(audio);
            if (transcript == null || transcript.isBlank()) {
                throw new AiNotConfiguredException("Não foi possível transcrever o áudio enviado");
            }

            var response = chatClient.prompt()
                    .user(transcript)
                    .call()
                    .content();
            if (response == null || response.isBlank()) {
                throw new AiNotConfiguredException("A IA não retornou uma resposta para o comando");
            }

            var generatedAudio = textToSpeechModel.call(response);
            if (generatedAudio == null || generatedAudio.length == 0) {
                throw new AiNotConfiguredException("A IA não retornou o áudio da resposta");
            }
            return new VoiceAssistantResponse(transcript, response, generatedAudio);
        } catch (AiNotConfiguredException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.warn("Falha ao processar comando de voz com OpenAI", exception);
            throw new AiNotConfiguredException(
                    "Não foi possível processar o comando de voz no momento", exception);
        }
    }
}
