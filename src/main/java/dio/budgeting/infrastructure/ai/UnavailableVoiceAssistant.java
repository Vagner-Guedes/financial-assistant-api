package dio.budgeting.infrastructure.ai;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
@Profile("!openai")
public class UnavailableVoiceAssistant implements VoiceAssistant {
    @Override
    public VoiceAssistantResponse process(Resource audio) {
        throw new AiNotConfiguredException(
                "A assistente de voz está desativada. Inicie a aplicação com o perfil 'openai' e configure OPENAI_API_KEY.");
    }
}
