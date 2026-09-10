package dio.budgeting.infrastructure.ai;

public record VoiceAssistantResponse(String transcript, String text, byte[] audio) {
}
