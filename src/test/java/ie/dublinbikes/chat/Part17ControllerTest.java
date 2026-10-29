package ie.dublinbikes.chat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ie.dublinbikes.chat.ChatException.Reason;
import ie.dublinbikes.chat.web.ChatController;

/** Part 17.3: the chat endpoint and its error responses (429 must carry a Retry-After header). */
@WebMvcTest(ChatController.class)
class Part17ControllerTest {

    static final String BODY = "{\"messages\":[{\"role\":\"user\",\"content\":\"Any bikes at Smithfield?\"}]}";

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ChatService chatService;

    @Test
    @DisplayName("17.3 POST /api/v1/chat: returns {\"reply\": ...} (the format the original frontend uses)")
    void chat() throws Exception {
        when(chatService.reply(List.of(ChatMessage.user("Any bikes at Smithfield?")))).thenReturn("Yes, 3 bikes.");

        mvc.perform(post("/api/v1/chat").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("Yes, 3 bikes."));
    }

    @Test
    @DisplayName("17.3 messages is an empty array: 400")
    void emptyMessages() throws Exception {
        mvc.perform(post("/api/v1/chat").contentType(MediaType.APPLICATION_JSON).content("{\"messages\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("17.3 Service throws IllegalArgumentException (no user message): 400")
    void noUserMessage() throws Exception {
        when(chatService.reply(any())).thenThrow(new IllegalArgumentException("At least one non-empty user message is required"));

        mvc.perform(post("/api/v1/chat").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @DisplayName("17.3 Rate limited: 429 with a Retry-After header, and retryAfterSeconds in the ProblemDetail")
    void rateLimited() throws Exception {
        when(chatService.reply(any())).thenThrow(new ChatException(Reason.RATE_LIMITED, "Groq rate limit reached", 12));

        mvc.perform(post("/api/v1/chat").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "12"))
                .andExpect(jsonPath("$.retryAfterSeconds").value(12));
    }

    @Test
    @DisplayName("17.3 No key configured / Groq error: 503 with title \"Chat unavailable\"")
    void unavailable() throws Exception {
        when(chatService.reply(any())).thenThrow(new ChatException(Reason.NOT_CONFIGURED, "GROQ_API_KEY is not set"));

        mvc.perform(post("/api/v1/chat").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.title").value("Chat unavailable"));
    }
}
