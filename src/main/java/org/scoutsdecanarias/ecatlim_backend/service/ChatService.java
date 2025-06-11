package org.scoutsdecanarias.ecatlim_backend.service;

import org.scoutsdecanarias.ecatlim_backend.entity.Chat;
import org.scoutsdecanarias.ecatlim_backend.entity.ChatMessage;
import org.scoutsdecanarias.ecatlim_backend.entity.User;
import org.scoutsdecanarias.ecatlim_backend.repository.ChatMessageRepository;
import org.scoutsdecanarias.ecatlim_backend.repository.ChatRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRepository chatRepository;
    private final UserService userService;

    public ChatService(ChatMessageRepository chatMessageRepository, ChatRepository chatRepository, UserService userService) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatRepository = chatRepository;
        this.userService = userService;
    }

    public ChatMessage saveChatMessage(ChatMessage msg) {
        msg.setTimestamp(ZonedDateTime.now());
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

    public List<ChatMessage> getChatHistory(Chat chat) {
        return chatMessageRepository.findByChatOrderByTimestampAsc((chat));
    }

    public List<Chat> getAllMyChats(){

        List<User> users = new ArrayList<>();
        users.add(userService.getUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()));

        return chatRepository.findByChatMembersContains(users);
    }

    public void saveChat(Chat chat){
        List<User> chatMembers = chat.getChatMembers();
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        chatMembers.add(userService.getUserByEmail(userName));

        chat.setCreationDate(Date.valueOf(ZonedDateTime.now().toLocalDate()));
        chat.setChatMembers(chatMembers);

        chatRepository.save(chat);
    }

    public void updateChat(Chat chat){
        Chat chatToUpdate = chatRepository.findById(chat.getId()).orElseThrow();

        chatToUpdate.setChatDescription(chat.getChatDescription());
        chatToUpdate.setChatName(chat.getChatName());
        chatToUpdate.setChatMembers(chat.getChatMembers());

        chatRepository.save(chat);
    }

    public void deleteChat(Chat chat){
        chatRepository.delete(chat);
    }

    public Chat getChatById(Integer id) {
        return chatRepository.findById(id).orElseThrow();
    }
}