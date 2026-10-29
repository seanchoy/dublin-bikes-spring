package ie.dublinbikes.chat.web;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

import ie.dublinbikes.chat.ChatMessage;

/** Request body for POST /api/v1/chat: the whole conversation (the frontend sends the full history every time). Provided. */
public record ChatRequest(@NotEmpty List<ChatMessage> messages) {
}
