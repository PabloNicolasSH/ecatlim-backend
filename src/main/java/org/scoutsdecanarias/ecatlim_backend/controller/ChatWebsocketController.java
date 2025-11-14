package org.scoutsdecanarias.ecatlim_backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.dto.ChatMessageDto;
import org.scoutsdecanarias.ecatlim_backend.entity.ChatMessage;
import org.scoutsdecanarias.ecatlim_backend.service.ChatService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Controller
public class ChatWebsocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate template;

    public ChatWebsocketController(ChatService chatService, SimpMessagingTemplate template) {
        this.chatService = chatService;
        this.template = template;
    }

    public record IncomingMessagePayload(String message) {}

    @MessageMapping("/chat/{chatId}/send")
    public void sendMessage(@DestinationVariable Integer chatId,
                            IncomingMessagePayload payload,
                            @Header("simpSessionAttributes") Map<String, Object> sessionAttrs) {

        String email = (String) sessionAttrs.get("username");

        if (email == null) {
            throw new org.springframework.security.access.AccessDeniedException("User not authenticated for WebSocket");
        }

        ChatMessage saved = chatService.saveChatMessage(chatId, email, payload.message());
        ChatMessageDto dto = ChatMessageDto.fromEntity(saved);

        template.convertAndSend("/topic/chat/" + chatId, dto);
    }
}
