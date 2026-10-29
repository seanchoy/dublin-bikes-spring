package ie.dublinbikes.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

import ie.dublinbikes.chat.ChatException.Reason;

/** Part 16: calling Groq (auth header, POST body, 429 rate limiting). */
class Part16GroqClientTest {

    static final String URL = "https://groq.test/v1/chat/completions";
    static final List<ChatMessage> MESSAGES = List.of(ChatMessage.system("You help with Dublin Bikes."),
            ChatMessage.user("Any bikes at Smithfield?"));
    static final String REPLY = "{\"id\":\"x\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\","
            + "\"content\":\"Smithfield North has 3 bikes.\"},\"finish_reason\":\"stop\"}]}";

    @Nested
    @RestClientTest(GroqClient.class)
    @Import(ChatConfig.class)
    @TestPropertySource(properties = {"app.groq.api-key=test-key", "app.groq.base-url=https://groq.test/v1",
            "app.groq.model=test-model"})
    @DisplayName("Key configured")
    class Configured {

        @Autowired
        GroqClient client;

        @Autowired
        MockRestServiceServer server;

        @Test
        @DisplayName("16.1 / 16.2 POST /chat/completions: Bearer auth header, snake_case body, returns the first reply's content")
        void completes() {
            server.expect(requestTo(URL))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-key"))
                    .andExpect(jsonPath("$.model").value("test-model"))
                    .andExpect(jsonPath("$.max_tokens").value(800))
                    .andExpect(jsonPath("$.temperature").value(0.7))
                    .andExpect(jsonPath("$.messages[0].role").value("system"))
                    .andExpect(jsonPath("$.messages[1].content").value("Any bikes at Smithfield?"))
                    .andRespond(withSuccess(REPLY, MediaType.APPLICATION_JSON));

            assertThat(client.complete(MESSAGES)).isEqualTo("Smithfield North has 3 bikes.");
            server.verify();
        }

        @Test
        @DisplayName("16.3 429 with Retry-After: 12: RATE_LIMITED, retryAfterSeconds = 12")
        void rateLimitedWithHeader() {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.RETRY_AFTER, "12");
            server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS).headers(headers));

            assertThatThrownBy(() -> client.complete(MESSAGES))
                    .isInstanceOfSatisfying(ChatException.class, e -> {
                        assertThat(e.getReason()).isEqualTo(Reason.RATE_LIMITED);
                        assertThat(e.getRetryAfterSeconds()).isEqualTo(12);
                    });
        }

        @Test
        @DisplayName("16.3 429 without Retry-After: defaults to 5 seconds")
        void rateLimitedWithoutHeader() {
            server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

            assertThatThrownBy(() -> client.complete(MESSAGES))
                    .isInstanceOfSatisfying(ChatException.class,
                            e -> assertThat(e.getRetryAfterSeconds()).isEqualTo(5));
        }

        @Test
        @DisplayName("16.3 500 and network errors: UNAVAILABLE")
        void unavailable() {
            server.expect(requestTo(URL)).andRespond(withServerError());
            assertThatThrownBy(() -> client.complete(MESSAGES))
                    .isInstanceOfSatisfying(ChatException.class,
                            e -> assertThat(e.getReason()).isEqualTo(Reason.UNAVAILABLE));

            server.reset();
            server.expect(requestTo(URL)).andRespond(withException(new IOException("connection reset")));
            assertThatThrownBy(() -> client.complete(MESSAGES))
                    .isInstanceOfSatisfying(ChatException.class,
                            e -> assertThat(e.getReason()).isEqualTo(Reason.UNAVAILABLE));
        }
    }

    @Nested
    @RestClientTest(GroqClient.class)
    @Import(ChatConfig.class)
    @TestPropertySource(properties = {"app.groq.api-key=", "app.groq.base-url=https://groq.test/v1"})
    @DisplayName("No key configured")
    class NotConfigured {

        @Autowired
        GroqClient client;

        @Autowired
        MockRestServiceServer server;

        @Test
        @DisplayName("16.1 No key: NOT_CONFIGURED, and no request is sent at all")
        void noKey() {
            assertThatThrownBy(() -> client.complete(MESSAGES))
                    .isInstanceOfSatisfying(ChatException.class,
                            e -> assertThat(e.getReason()).isEqualTo(Reason.NOT_CONFIGURED));
            server.verify();
        }
    }
}
