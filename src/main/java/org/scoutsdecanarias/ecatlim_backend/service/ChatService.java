package org.scoutsdecanarias.ecatlim_backend.service;

import org.scoutsdecanarias.ecatlim_backend.entity.ChatMessage;
import org.scoutsdecanarias.ecatlim_backend.repository.ChatMessageRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(ChatMessageRepository chatMessageRepository, SimpMessagingTemplate messagingTemplate) {
        this.chatMessageRepository = chatMessageRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public void saveAndSend(ChatMessage msg) {
        msg.setTimestamp(LocalDateTime.now());
        chatMessageRepository.save(msg);
        messagingTemplate.convertAndSend("/topic/messages", msg);
    }

    public List<ChatMessage> getChatHistory(String user1, String user2) {
        return chatMessageRepository.findByFromUserAndToUserOrFromUserAndToUser(user1, user2, user2, user1);
    }
}