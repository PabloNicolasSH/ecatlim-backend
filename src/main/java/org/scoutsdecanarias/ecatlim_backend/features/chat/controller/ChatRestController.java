package org.scoutsdecanarias.ecatlim_backend.features.chat.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.scoutsdecanarias.ecatlim_backend.features.chat.Chat;
import org.scoutsdecanarias.ecatlim_backend.features.chat.ChatMessage;
import org.scoutsdecanarias.ecatlim_backend.features.chat.dto.ChatDto;
import org.scoutsdecanarias.ecatlim_backend.features.chat.dto.ChatMessageDto;
import org.scoutsdecanarias.ecatlim_backend.features.chat.dto.NewChatFormDto;
import org.scoutsdecanarias.ecatlim_backend.features.chat.repository.ChatRepository;
import org.scoutsdecanarias.ecatlim_backend.features.chat.service.ChatService;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    private final SimpMessagingTemplate template;

    public ChatRestController(ChatService chatService, ChatRepository chatRepository, UserService userService, SimpMessagingTemplate template) {
        this.chatService = chatService;
        this.chatRepository = chatRepository;
        this.userService = userService;
        this.template = template;
    }


    @GetMapping("/{id}/messages")
    public List<ChatMessageDto> getChatMessages(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            Principal principal
    ) {
        log.info("METHOD getChatMessages() - chat {} page {} size {}", id, page, size);

        Page<ChatMessage> pageResult = chatService.getChatHistoryPage(id, principal.getName(), page, size);

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

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void addChat(@RequestPart("chat") @Valid NewChatFormDto chat,
                        @RequestPart(value = "picture", required = false) @Nullable MultipartFile picture,
                        Principal principal) {
        log.info("METHOD addChat() - Adding new chat");
        Chat saved = chatService.saveChat(chat, picture);

        ChatDto dto = ChatDto.fromEntity(saved, null);
        saved.getChatMembers().stream()
                .map(User::getEmail)
                .filter(email -> !email.equals(principal.getName()))
                .forEach(email -> template.convertAndSendToUser(email, "/queue/new-chats", dto));
    }

    @PostMapping(value = "/{id}/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ChatDto setPicture(@PathVariable Integer id, @RequestPart("picture") MultipartFile picture, Principal principal) {
        log.info("METHOD setPicture() - User {} changing picture of chat {}", principal.getName(), id);
        Chat saved = chatService.setChatPicture(id, principal.getName(), picture);
        return broadcastChatUpdated(saved);
    }

    @DeleteMapping("/{id}/picture")
    public ChatDto removePicture(@PathVariable Integer id, Principal principal) {
        log.info("METHOD removePicture() - User {} removing picture of chat {}", principal.getName(), id);
        Chat saved = chatService.removeChatPicture(id, principal.getName());
        return broadcastChatUpdated(saved);
    }

    private ChatDto broadcastChatUpdated(Chat chat) {
        ChatDto dto = ChatDto.fromEntity(chat, null);
        template.convertAndSend("/topic/chat/" + chat.getId() + "/updated", dto);
        return dto;
    }

    @DeleteMapping("/{id}/messages/{messageId}")
    public void deleteMessage(@PathVariable Integer id, @PathVariable Integer messageId, Principal principal) {
        log.info("METHOD deleteMessage() - Deleting message {} from chat {}", messageId, id);
        chatService.deleteChatMessage(id, messageId, principal.getName());
        template.convertAndSend("/topic/chat/" + id + "/message-deleted", messageId);
    }

    @DeleteMapping("/{id}")
    public void leaveChat(@PathVariable Integer id, Principal principal) {
        log.info("METHOD leaveChat() - User {} leaving chat {}", principal.getName(), id);
        chatService.leaveChat(id, principal.getName())
                .ifPresent(msg -> template.convertAndSend("/topic/chat/" + id, ChatMessageDto.fromEntity(msg)));
    }

    @PostMapping("/{id}/mark-read")
    public void markRead(@PathVariable Integer id) {
        log.info("METHOD markRead() - Marking chat {} as read", id);
        chatService.markChatAsRead(id);
    }
}
