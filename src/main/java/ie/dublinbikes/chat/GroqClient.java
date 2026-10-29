package ie.dublinbikes.chat;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClientException;

/** Part 16: see workbook Part 16 */
// TODO 16.1: let Spring manage this class
@Component
public class GroqClient {

    private final RestClient restClient;
    private final GroqProperties properties;

    public GroqClient(RestClient.Builder builder, GroqProperties properties) {
        this.properties = properties;
        // TODO 16.1: build the RestClient from builder, with baseUrl properties.baseUrl()
        this.restClient = builder.baseUrl(properties.baseUrl()).build();
    }

    public String complete(List<ChatMessage> messages) {
        // TODO 16.1: if apiKey is not configured (null or blank), throw ChatException(NOT_CONFIGURED, ...) without sending a request
        if(properties.apiKey().isEmpty()){
            throw new ChatException(ChatException.Reason.NOT_CONFIGURED, "Chat API Key is not configured");
        }
        // TODO 16.2: POST /chat/completions with header Authorization: Bearer {apiKey},
        //            body GroqDto.CompletionRequest(model, messages, maxTokens, temperature),
        //            return choices[0].message.content

        // TODO 16.3: 429 → ChatException(RATE_LIMITED, message, Retry-After seconds, or 5 if missing or not a number);
        //            other 4xx/5xx and network errors → ChatException(UNAVAILABLE, ...)


        try{
            GroqDto.CompletionRequest request = new GroqDto.CompletionRequest(properties.model(), messages, properties.maxTokens(), properties.temperature());

            GroqDto.CompletionResponse response = restClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .onStatus(status -> status.value() == 429, (req, res) -> {
                        String retryAfterHeader = res.getHeaders().getFirst(HttpHeaders.RETRY_AFTER);
                        int retryAfter;
                        if(retryAfterHeader == null){
                             retryAfter = 5;
                        }else{
                            try{
                                retryAfter = Integer.parseInt(retryAfterHeader.trim());
                            } catch (NumberFormatException e){
                                retryAfter = 5;
                            }
                        }
                        throw new ChatException(ChatException.Reason.RATE_LIMITED, "Rate Limit reach", retryAfter);
                    })
                    .body(GroqDto.CompletionResponse.class);

            return response.choices().get(0).message().content();
        } catch (RestClientException e) {
            throw new ChatException(ChatException.Reason.UNAVAILABLE, e.getMessage());
        }
    }

}
