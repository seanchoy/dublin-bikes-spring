package ie.dublinbikes.chat;

import java.util.List;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/** Request and response for Groq (OpenAI-compatible) /chat/completions. Provided. */
public final class GroqDto {

    private GroqDto() {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record CompletionRequest(String model, List<ChatMessage> messages, int maxTokens, double temperature) {
    }

    public record Choice(ChatMessage message) {
    }

    public record CompletionResponse(List<Choice> choices) {
    }
}
