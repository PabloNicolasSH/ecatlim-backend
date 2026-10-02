package org.scoutsdecanarias.ecatlim_backend.features.chat.service;

import jakarta.transaction.Transactional;
import org.jspecify.annotations.Nullable;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.scoutsdecanarias.ecatlim_backend.features.chat.Chat;
import org.scoutsdecanarias.ecatlim_backend.features.chat.ChatMessage;
import org.scoutsdecanarias.ecatlim_backend.features.chat.dto.ChatDto;
import org.scoutsdecanarias.ecatlim_backend.features.chat.dto.NewChatFormDto;
import org.scoutsdecanarias.ecatlim_backend.features.chat.enums.ChatMessageType;
import org.scoutsdecanarias.ecatlim_backend.features.chat.exception.ChatException;
import org.scoutsdecanarias.ecatlim_backend.features.chat.repository.ChatMessageRepository;
import org.scoutsdecanarias.ecatlim_backend.features.chat.repository.ChatRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.service.UserService;
import org.scoutsdecanarias.ecatlim_backend.features.user_file.UserFile;
import org.scoutsdecanarias.ecatlim_backend.features.user_file.UserFileService;
import org.scoutsdecanarias.ecatlim_backend.features.user_file.UserFileType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Duration MESSAGE_DELETION_WINDOW = Duration.ofMinutes(15);

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRepository chatRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final UserFileService userFileService;

    public ChatService(ChatMessageRepository chatMessageRepository, ChatRepository chatRepository, UserService userService, UserRepository userRepository, UserFileService userFileService) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatRepository = chatRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.userFileService = userFileService;
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

    public void deleteChatMessage(Integer chatId, Integer messageId, String requesterEmail) {
        assertMember(chatId, requesterEmail);

        ChatMessage msg = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> ChatException.messageNotFound(chatId));
        if (!msg.getChat().getId().equals(chatId)
                || msg.getType() != ChatMessageType.TEXT
                || !msg.getFrom().getEmail().equals(requesterEmail)) {
            throw ChatException.messageNotDeletable(chatId);
        }
        if (msg.getTimestamp().isBefore(ZonedDateTime.now().minus(MESSAGE_DELETION_WINDOW))) {
            throw ChatException.messageDeletionExpired(chatId, MESSAGE_DELETION_WINDOW.toMinutes());
        }

        chatMessageRepository.delete(msg);
    }

    public Map<Integer, Long> getUnreadMessagesCount(User user) {
        List<Object[]> results = chatMessageRepository.countUnreadMessagesByChatForUser(user);
        return results.stream()
                .collect(Collectors.toMap(
                        r -> (Integer) r[0],
                        r -> (Long) r[1]
                ));
    }

    public Page<ChatMessage> getChatHistoryPage(Integer chatId, String requesterEmail, int page, int size) {
        assertMember(chatId, requesterEmail);

        Chat chat = getChatById(chatId);
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

    @Transactional
    public Chat saveChat(NewChatFormDto chatDto, @Nullable MultipartFile picture){
        Chat chat = new Chat();
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        List<User> chatMembers = userRepository.findAllById(chatDto.chatMembers());
        chatMembers.add(userService.getUserByEmail(userName));

        chat.setChatMembers(chatMembers);
        chat.setCreationDate(ZonedDateTime.now().toInstant());
        chat.setChatName(chatDto.name());
        chat.setChatDescription(chatDto.description());

        if (picture != null && !picture.isEmpty()) {
            assertImage(picture);
            chat.setChatPicture(userFileService.storeFile(picture, UserFileType.CHAT_PICTURE, "chat_" + chatDto.name()));
        }

        return chatRepository.save(chat);
    }

    @Transactional
    public Chat setChatPicture(Integer chatId, String requesterEmail, MultipartFile picture) {
        Chat chat = getGroupForMember(chatId, requesterEmail);
        assertImage(picture);

        UserFile previous = chat.getChatPicture();
        chat.setChatPicture(userFileService.storeFile(picture, UserFileType.CHAT_PICTURE, "chat_" + chat.getChatName()));
        Chat saved = chatRepository.save(chat);

        if (previous != null) {
            userFileService.deleteStoredFile(previous);
        }
        return saved;
    }

    @Transactional
    public Chat removeChatPicture(Integer chatId, String requesterEmail) {
        Chat chat = getGroupForMember(chatId, requesterEmail);

        UserFile previous = chat.getChatPicture();
        if (previous == null) return chat;

        chat.setChatPicture(null);
        Chat saved = chatRepository.save(chat);
        userFileService.deleteStoredFile(previous);
        return saved;
    }

    private Chat getGroupForMember(Integer chatId, String email) {
        assertMember(chatId, email);
        Chat chat = getChatById(chatId);
        if (chat.getChatName() == null) {
            throw new EcatlimException("Solo los grupos pueden tener foto", HttpStatus.BAD_REQUEST);
        }
        return chat;
    }

    private void assertImage(MultipartFile file) {
        if (file.isEmpty() || file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new EcatlimException("El archivo debe ser una imagen", HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public Optional<ChatMessage> leaveChat(Integer chatId, String requesterEmail) {
        assertMember(chatId, requesterEmail);

        Chat chat = getChatById(chatId);
        User leaver = userService.getUserByEmail(requesterEmail);
        chat.getChatMembers().removeIf(member -> requesterEmail.equals(member.getEmail()));

        if (chat.getChatMembers().isEmpty()) {
            chatMessageRepository.deleteAllByChat(chat);
            if (chat.getChatPicture() != null) {
                userFileService.deleteStoredFile(chat.getChatPicture());
            }
            chatRepository.delete(chat);
            return Optional.empty();
        }

        chatRepository.save(chat);

        String name = leaver.getProfile() != null ? leaver.getProfile().getName() : leaver.getEmail();

        ChatMessage msg = new ChatMessage();
        msg.setChat(chat);
        msg.setFrom(leaver);
        msg.setType(ChatMessageType.USER_LEFT);
        msg.setMessage(name + " ha salido del chat");
        msg.setTimestamp(ZonedDateTime.now());
        msg.setRead(true);

        return Optional.of(chatMessageRepository.save(msg));
    }

    public List<String> getMemberEmailsExcept(Integer chatId, String email) {
        return chatRepository.findMemberEmails(chatId).stream()
                .filter(memberEmail -> !memberEmail.equals(email))
                .toList();
    }

    public void assertMember(Integer chatId, String email) {
        if (!chatRepository.existsById(chatId)) {
            throw ChatException.chatNotFound(chatId);
        }
        if (!chatRepository.existsByIdAndMemberEmail(chatId, email)) {
            throw ChatException.notAMember(chatId);
        }
    }

    @Transactional
    public void markChatAsRead(Integer chatId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        assertMember(chatId, email);

        User user = userService.getUserByEmail(email);
        Chat chat = chatRepository.findById(chatId).orElseThrow();

        chatMessageRepository.markMessagesAsReadForUser(
                chat,
                user,
                ZonedDateTime.now()
        );
    }
}