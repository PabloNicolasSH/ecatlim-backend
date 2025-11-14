package org.scoutsdecanarias.ecatlim_backend.dto;

import org.scoutsdecanarias.ecatlim_backend.entity.ChatMessage;

import java.util.List;

public record ChatMessageDto(
        Integer id,
        SimpleUserDto from,
        Integer chatId,
        String message,
        String timestamp,
        boolean read
) {
    public static ChatMessageDto fromEntity(ChatMessage chatMessage) {
        return new ChatMessageDto(
                chatMessage.getId(),
                SimpleUserDto.fromEntity(chatMessage.getFrom()),
                chatMessage.getChat().getId(),
                chatMessage.getMessage(),
                chatMessage.getTimestamp().toString(),
                chatMessage.isRead()
        );
    }

    public static List<ChatMessageDto> fromCollection(List<ChatMessage> chatHistory) {
        return chatHistory.stream()
                .map(ChatMessageDto::fromEntity)
                .toList();
    }
}
