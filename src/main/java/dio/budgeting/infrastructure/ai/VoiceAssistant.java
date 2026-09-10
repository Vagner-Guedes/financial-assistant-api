package dio.budgeting.infrastructure.ai;

import org.springframework.core.io.Resource;

public interface VoiceAssistant {
    VoiceAssistantResponse process(Resource audio);
}
