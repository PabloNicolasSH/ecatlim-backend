package org.scoutsdecanarias.ecatlim_backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.dto.ChatMessageDto;
import org.scoutsdecanarias.ecatlim_backend.entity.ChatMessage;
import org.scoutsdecanarias.ecatlim_backend.service.ChatService;
import org.scoutsdecanarias.ecatlim_backend.service.UserService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Slf4j
@Controller
public class ChatWebsocketController {

    private final UserService userService;
    private final ChatService chatService;
    private final SimpMessagingTemplate template;

    public ChatWebsocketController(UserService userService, ChatService chatService, SimpMessagingTemplate template) {
        this.userService = userService;
        this.chatService = chatService;
        this.template = template;
    }

    @MessageMapping("/chat/{chatId}/send")
    @SendTo("/topic/chat/{chatId}")
    public ChatMessageDto sendMessage(@DestinationVariable Integer chatId, ChatMessageDto messageDto, @Header("simpSessionAttributes") Map<String, Object> attributes) {
        log.info("METHOD sendMessage() - Called by {}", attributes.get("username"));

        ChatMessage message = new ChatMessage();

        String username = (String) attributes.get("username");
        message.setFrom(userService.getUserByEmail(username));
        message.setMessage(messageDto.message());
        message.setChat(chatService.getChatById(messageDto.to()));

        return ChatMessageDto.fromEntity(chatService.saveChatMessage(message));
    }
}
