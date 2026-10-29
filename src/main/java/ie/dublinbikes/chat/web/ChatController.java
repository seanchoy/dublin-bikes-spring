package ie.dublinbikes.chat.web;

import ie.dublinbikes.chat.ChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Part 17.3: see workbook Part 17 */
// TODO 17.3: REST controller with base path /api/v1/chat
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // TODO 17.3: POST /api/v1/chat; the parameter comes from the JSON body and triggers validation on ChatRequest
    @PostMapping
    public ChatResponse chat(
            @Valid @RequestBody ChatRequest request) {

        return new ChatResponse(chatService.reply(request.messages()));
    };
}
