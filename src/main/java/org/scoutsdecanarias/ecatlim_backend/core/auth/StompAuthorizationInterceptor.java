package org.scoutsdecanarias.ecatlim_backend.core.auth;

import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.features.chat.repository.ChatRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class StompAuthorizationInterceptor implements ChannelInterceptor {

    private static final Pattern CHAT_TOPIC = Pattern.compile("^/topic/chat/(\\d+)(/[a-z-]+)?$");

    private final ChatRepository chatRepository;

    public StompAuthorizationInterceptor(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (command != StompCommand.SUBSCRIBE && command != StompCommand.SEND) {
            return message;
        }

        Principal user = accessor.getUser();
        String destination = accessor.getDestination();
        if (user == null || destination == null) {
            log.warn("METHOD preSend() - Denied {} without user or destination", command);
            return null;
        }

        boolean allowed = command == StompCommand.SUBSCRIBE
                ? canSubscribe(user.getName(), destination)
                : destination.startsWith("/app/");

        if (!allowed) {
            log.warn("METHOD preSend() - Denied {} to {} for user {}", command, destination, user.getName());
            return null;
        }
        return message;
    }

    private boolean canSubscribe(String email, String destination) {
        if (destination.startsWith("/user/")) {
            return true;
        }
        Matcher chatTopic = CHAT_TOPIC.matcher(destination);
        if (chatTopic.matches()) {
            Integer chatId = Integer.valueOf(chatTopic.group(1));
            return chatRepository.existsByIdAndMemberEmail(chatId, email);
        }
        return false;
    }
}
