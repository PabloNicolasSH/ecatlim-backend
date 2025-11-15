package org.scoutsdecanarias.ecatlim_backend.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.dto.Chat.ChatDto;
import org.scoutsdecanarias.ecatlim_backend.dto.Chat.ChatMessageDto;
import org.scoutsdecanarias.ecatlim_backend.dto.Chat.NewChatFormDto;
import org.scoutsdecanarias.ecatlim_backend.entity.Chat;
import org.scoutsdecanarias.ecatlim_backend.entity.ChatMessage;
import org.scoutsdecanarias.ecatlim_backend.entity.User;
import org.scoutsdecanarias.ecatlim_backend.repository.ChatRepository;
import org.scoutsdecanarias.ecatlim_backend.service.ChatService;
import org.scoutsdecanarias.ecatlim_backend.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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


    @GetMapping("/{id}/messages")
    public List<ChatMessageDto> getChatMessages(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        log.info("METHOD getChatMessages() - chat {} page {} size {}", id, page, size);

        Chat chat = chatRepository.getChatById(id);
        Page<ChatMessage> pageResult = chatService.getChatHistoryPage(chat, page, size);

        return ChatMessageDto.fromCollection(pageResult.getContent());
    }

    @GetMapping("/allMyChats")
    public List<ChatDto> getAllMyChats(Principal principal) {
        log.info("Method getAllMyChats() - Getting all chats for {}", principal.getName());
        return chatService.getAllMyChats();
    }

    @GetMapping("/unread-chats")
    public Map<Integer, Long> getUnreadCounts(){
        log.info("METHOD getUnreadCounts() - Getting unread counts for user {}", SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userService.getUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        return chatService.getUnreadMessagesCount(user);
    }

    @PostMapping("/add")
    public void addChat(@RequestBody @Valid NewChatFormDto chat) {
        log.info("METHOD addChat() - Adding new chat");
        chatService.saveChat(chat);
    }

    @PostMapping("/{id}/mark-read")
    public void markRead(@PathVariable Integer id) {
        log.info("METHOD markRead() - Marking chat {} as read", id);
        chatService.markChatAsRead(id);
    }
}
