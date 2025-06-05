package org.scoutsdecanarias.ecatlim_backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.entity.ChatMessage;
import org.scoutsdecanarias.ecatlim_backend.repository.ChatMessageRepository;
import org.scoutsdecanarias.ecatlim_backend.service.ChatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/messages")
public class ChatRestController {

    private final ChatService chatService;

    public ChatRestController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/{withUser}")
    public List<ChatMessage> getChat(@PathVariable String withUser, Principal principal) {
        log.info("METHOD getMessages() - All messages from or to {} got", principal.getName());
        return chatService.getChatHistory(principal.getName(), withUser);
    }
}
