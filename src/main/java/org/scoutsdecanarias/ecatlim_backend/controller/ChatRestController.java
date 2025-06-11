package org.scoutsdecanarias.ecatlim_backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.dto.ChatDto;
import org.scoutsdecanarias.ecatlim_backend.dto.ChatMessageDto;
import org.scoutsdecanarias.ecatlim_backend.entity.Chat;
import org.scoutsdecanarias.ecatlim_backend.entity.User;
import org.scoutsdecanarias.ecatlim_backend.repository.ChatRepository;
import org.scoutsdecanarias.ecatlim_backend.service.ChatService;
import org.scoutsdecanarias.ecatlim_backend.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatRestController {

    private final ChatService chatService;
    private final ChatRepository chatRepository;
    private final UserService userService;

    public ChatRestController(ChatService chatService, ChatRepository chatRepository, UserService userService) {
        this.chatService = chatService;
        this.chatRepository = chatRepository;
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public List<ChatMessageDto> getChat(@PathVariable String id, Principal principal) {
        log.info("METHOD getMessages() - All messages from or to {} got", principal.getName());

        Chat chat = chatRepository.getChatById(Integer.valueOf(id));

        return ChatMessageDto.fromCollection(chatService.getChatHistory(chat));
    }

    @GetMapping("/allMyChats")
    public List<ChatDto> getAllMyChats() {
        log.info("Method getAllMyChats() - Getting all chats");
        return ChatDto.fromCollection(chatService.getAllMyChats());
    }

    @GetMapping("/unread-chats")
    public Map<Integer, Long> getUnreadCounts(){
        log.info("METHOD getUnreadCounts() - Getting unread counts for user {}", SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userService.getUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        return chatService.getUnreadMessagesCount(user);
    }

    @PostMapping("/add")
    public void addChat(@RequestBody Chat chat) {
        log.info("METHOD addChat() - Adding new chat");
        chatService.saveChat(chat);
    }
}
