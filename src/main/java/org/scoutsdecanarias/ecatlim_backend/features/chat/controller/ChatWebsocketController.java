package org.scoutsdecanarias.ecatlim_backend.features.chat.controller;


import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.features.chat.ChatMessage;
import org.scoutsdecanarias.ecatlim_backend.features.chat.dto.ChatMessageDto;
import org.scoutsdecanarias.ecatlim_backend.features.chat.dto.ChatNotificationDto;
import org.scoutsdecanarias.ecatlim_backend.features.chat.exception.ChatException;
import org.scoutsdecanarias.ecatlim_backend.features.chat.service.ChatService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;

import java.util.LinkedHashMap;
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

    public record IncomingMessagePayload(String message, String clientId) {
    }

    @MessageMapping("/chat/{chatId}/send")
    public void sendMessage(@DestinationVariable Integer chatId,
                            IncomingMessagePayload payload,
                            @Header("simpSessionAttributes") Map<String, Object> sessionAttrs) {

        String email = (String) sessionAttrs.get("username");

        if (email == null) {
            throw new AccessDeniedException("User not authenticated for WebSocket");
        }

        ChatMessage saved = chatService.saveChatMessage(chatId, email, payload.message());
        ChatMessageDto dto = ChatMessageDto.fromEntity(saved, payload.clientId());

        template.convertAndSend("/topic/chat/" + chatId, dto);

        chatService.getMemberEmailsExcept(chatId, email).forEach(member ->
                template.convertAndSendToUser(member, "/queue/chat-notifications", new ChatNotificationDto(chatId)));
    }

    @MessageExceptionHandler(ChatException.class)
    @SendToUser("/queue/errors")
    public Map<String, Object> handleChatException(ChatException e) {
        log.warn("METHOD handleChatException() - {} in chat {}", e.getCode(), e.getChatId());
        return toPayload(e);
    }

    @MessageExceptionHandler(Exception.class)
    @SendToUser("/queue/errors")
    public Map<String, Object> handleUnexpectedException(Exception e) {
        log.error("METHOD handleUnexpectedException() - Error handling websocket message", e);
        return toPayload(ChatException.unknown(null));
    }

    private Map<String, Object> toPayload(ChatException e) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("chatId", e.getChatId());
        payload.put("code", e.getCode().name());
        payload.put("message", e.getMessage());
        return payload;
    }
}
