package org.scoutsdecanarias.ecatlim_backend.features.chat.dto;

import org.scoutsdecanarias.ecatlim_backend.features.chat.Chat;
import org.scoutsdecanarias.ecatlim_backend.features.chat.ChatMessage;
import org.scoutsdecanarias.ecatlim_backend.features.chat.enums.ChatMessageType;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.SimpleUserDto;

import java.util.List;

public record ChatDto(
    Integer id,
    String name,
    String description,
    String creationDate,
    String pictureUrl,
    List<SimpleUserDto> chatMembers,
    String lastMessagePreview,
    String lastMessageAt,
    SimpleUserDto lastMessageFrom,
    ChatMessageType lastMessageType
) {
    public static ChatDto fromEntity(Chat chat, ChatMessage lastMessage) {

        String preview = null;
        String lastAt = null;
        SimpleUserDto lastFrom = null;
        ChatMessageType lastType = null;

        if (lastMessage != null) {
            String raw = lastMessage.getMessage();

            preview = raw.length() > 40 ? raw.substring(0, 40) + "…" : raw;
            lastAt = lastMessage.getTimestamp().toString();
            lastFrom = SimpleUserDto.fromEntity(lastMessage.getFrom());
            lastType = lastMessage.getType();
        }

        return new ChatDto(
            chat.getId(),
            chat.getChatName(),
            chat.getChatDescription(),
            chat.getCreationDate() != null ? chat.getCreationDate().toString() : null,
            chat.getChatPicture() != null ? "/users/me/files/" + chat.getChatPicture().getId() : null,
            chat.getChatMembers().stream().map(SimpleUserDto::fromEntity).toList(),
            preview,
            lastAt,
            lastFrom,
            lastType
        );
    }
}
