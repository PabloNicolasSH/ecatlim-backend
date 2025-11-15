package org.scoutsdecanarias.ecatlim_backend.dto.Chat;

import org.scoutsdecanarias.ecatlim_backend.dto.SimpleUserDto;
import org.scoutsdecanarias.ecatlim_backend.entity.Chat;
import org.scoutsdecanarias.ecatlim_backend.entity.ChatMessage;

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
                SimpleUserDto.fromCollection(chat.getChatMembers()),
                preview,
                lastAt
        );
    }
}
