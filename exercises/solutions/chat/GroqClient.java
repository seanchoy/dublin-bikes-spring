package ie.dublinbikes.chat;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import ie.dublinbikes.chat.ChatException.Reason;

@Component
public class GroqClient {

    static final int DEFAULT_RETRY_AFTER_SECONDS = 5;

    private final RestClient restClient;
    private final GroqProperties properties;

    public GroqClient(RestClient.Builder builder, GroqProperties properties) {
        this.properties = properties;
        this.restClient = builder.baseUrl(properties.baseUrl()).build();
    }

    public String complete(List<ChatMessage> messages) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new ChatException(Reason.NOT_CONFIGURED, "GROQ_API_KEY is not set");
        }
        GroqDto.CompletionRequest request = new GroqDto.CompletionRequest(
                properties.model(), messages, properties.maxTokens(), properties.temperature());
        try {
            GroqDto.CompletionResponse response = restClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .onStatus(status -> status.value() == HttpStatus.TOO_MANY_REQUESTS.value(), (req, res) -> {
                        throw new ChatException(Reason.RATE_LIMITED, "Groq rate limit reached",
                                retryAfter(res.getHeaders().getFirst(HttpHeaders.RETRY_AFTER)));
                    })
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new ChatException(Reason.UNAVAILABLE, "Groq returned HTTP " + res.getStatusCode().value());
                    })
                    .body(GroqDto.CompletionResponse.class);
            return response.choices().get(0).message().content();
        } catch (RestClientException e) {
            throw new ChatException(Reason.UNAVAILABLE, "Could not reach Groq: " + e.getMessage());
        }
    }

    static int retryAfter(String header) {
        if (header == null) {
            return DEFAULT_RETRY_AFTER_SECONDS;
        }
        try {
            return Integer.parseInt(header.trim());
        } catch (NumberFormatException e) {
            return DEFAULT_RETRY_AFTER_SECONDS;
        }
    }
}
