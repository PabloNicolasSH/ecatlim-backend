package org.scoutsdecanarias.ecatlim_backend.features.chat.dto;

import org.scoutsdecanarias.ecatlim_backend.features.chat.Chat;
import org.scoutsdecanarias.ecatlim_backend.features.chat.ChatMessage;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.SimpleUserDto;

import java.util.List;

public record ChatDto(
    Integer id,
    String name,
    String description,
    List<SimpleUserDto> chatMembers,
    String lastMessagePreview,
    String lastMessageAt
) {
    public static ChatDto fromEntity(Chat chat, ChatMessage lastMessage) {

        String preview = null;
        String lastAt = null;

        if (lastMessage != null) {
            String raw = lastMessage.getMessage();

            preview = raw.length() > 40 ? raw.substring(0, 40) + "…" : raw;
            lastAt = lastMessage.getTimestamp().toString();
        }

        return new ChatDto(
            chat.getId(),
            chat.getChatName(),
            chat.getChatDescription(),
            chat.getChatMembers().stream().map(SimpleUserDto::fromEntity).toList(),
            preview,
            lastAt
        );
    }
}
