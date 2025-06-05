package org.scoutsdecanarias.ecatlim_backend.controller;

import org.scoutsdecanarias.ecatlim_backend.entity.ChatMessage;
import org.scoutsdecanarias.ecatlim_backend.service.ChatService;
import org.scoutsdecanarias.ecatlim_backend.service.UserService;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class ChatWebsocketController {

    private final UserService userService;
    private final ChatService chatService;

    public ChatWebsocketController(UserService userService, ChatService chatService) {
        this.userService = userService;
        this.chatService = chatService;
    }

    @MessageMapping("/chat.send")
    @SendTo("/topic/messages")
    public void sendMessage(ChatMessage message, @Header("simpSessionAttributes") Map<String, Object> attributes) {
        String username = (String) attributes.get("username");
        message.setFrom(userService.getUserByEmail(username));
        chatService.saveAndSend(message);
    }
}
