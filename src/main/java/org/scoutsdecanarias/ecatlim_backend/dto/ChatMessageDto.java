package org.scoutsdecanarias.ecatlim_backend.dto;

import org.scoutsdecanarias.ecatlim_backend.entity.ChatMessage;

import java.util.List;

public record ChatMessageDto(
        String from,
        Integer to,
        String message,
        String timestamp
) {
    public static ChatMessageDto fromEntity(ChatMessage chatMessage) {
        return new ChatMessageDto(
                chatMessage.getFrom().getEmail(),
                chatMessage.getChat().getId(),
                chatMessage.getMessage(),
                chatMessage.getTimestamp().toString()
        );
    }

    public static List<ChatMessageDto> fromCollection(List<ChatMessage> chatHistory) {
        return chatHistory.stream().map(ChatMessageDto::fromEntity).toList();
    }
}
