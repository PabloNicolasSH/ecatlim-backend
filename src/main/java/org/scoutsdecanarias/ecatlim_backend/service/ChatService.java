package org.scoutsdecanarias.ecatlim_backend.service;

import jakarta.transaction.Transactional;
import org.scoutsdecanarias.ecatlim_backend.dto.Chat.ChatDto;
import org.scoutsdecanarias.ecatlim_backend.dto.Chat.NewChatFormDto;
import org.scoutsdecanarias.ecatlim_backend.entity.Chat;
import org.scoutsdecanarias.ecatlim_backend.entity.ChatMessage;
import org.scoutsdecanarias.ecatlim_backend.entity.User;
import org.scoutsdecanarias.ecatlim_backend.repository.ChatMessageRepository;
import org.scoutsdecanarias.ecatlim_backend.repository.ChatRepository;
import org.scoutsdecanarias.ecatlim_backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRepository chatRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    public ChatService(ChatMessageRepository chatMessageRepository, ChatRepository chatRepository, UserService userService, UserRepository userRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatRepository = chatRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    public ChatMessage saveChatMessage(Integer chatId, String senderEmail, String content) {
        User sender = userService.getUserByEmail(senderEmail);
        assertMember(chatId, senderEmail);

        Chat chat = getChatById(chatId);

        ChatMessage msg = new ChatMessage();
        msg.setChat(chat);
        msg.setFrom(sender);
        msg.setMessage(content);
        msg.setTimestamp(ZonedDateTime.now());
        msg.setRead(false);

        return chatMessageRepository.save(msg);
    }

    public void deleteChatMessage(ChatMessage msg) {
        msg.setDeleted(true);
        chatMessageRepository.save(msg);
    }

    public Map<Integer, Long> getUnreadMessagesCount(User user) {
        List<Object[]> results = chatMessageRepository.countUnreadMessagesByChatForUser(user);
        return results.stream()
                .collect(Collectors.toMap(
                        r -> (Integer) r[0],
                        r -> (Long) r[1]
                ));
    }

    public Page<ChatMessage> getChatHistoryPage(Chat chat, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return chatMessageRepository.findByChat(chat, pageable);
    }

    public List<ChatDto> getAllMyChats(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Chat> chats = chatRepository.findAllByMemberEmail(email);

        return chats.stream()
                .map(chat -> {
                    ChatMessage lastMessage = chatMessageRepository.findTopByChatOrderByTimestampDesc(chat);
                    return ChatDto.fromEntity(chat, lastMessage);
                })
                .toList();
    }

    public Chat getChatById(Integer id) {
        return chatRepository.findById(id).orElseThrow();
    }

    public void saveChat(NewChatFormDto chatDto){
        Chat chat = new Chat();
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        List<User> chatMembers = userRepository.findAllById(chatDto.chatMembers());
        chatMembers.add(userService.getUserByEmail(userName));

        chat.setChatMembers(chatMembers);
        chat.setCreationDate(Date.from(ZonedDateTime.now().toInstant()));
        chat.setChatName(chatDto.name());
        chat.setChatDescription(chatDto.description());

        chatRepository.save(chat);
    }

    public void deleteChat(Chat chat){
        chatRepository.delete(chat);
    }

    public void assertMember(Integer chatId, String email) {
        boolean member = chatRepository.existsByIdAndMemberEmail(chatId, email);
        if (!member) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "The user " + email + " doesn't belong to chat " + chatId
            );
        }
    }

    @Transactional
    public void markChatAsRead(Integer chatId) {
        User user = userService.getUserByEmail(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
        Chat chat = chatRepository.findById(chatId).orElseThrow();

        chatMessageRepository.markMessagesAsReadForUser(
                chat,
                user,
                ZonedDateTime.now()
        );
    }
}