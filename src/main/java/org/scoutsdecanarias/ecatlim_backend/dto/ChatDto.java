package org.scoutsdecanarias.ecatlim_backend.dto;

import org.scoutsdecanarias.ecatlim_backend.entity.Chat;

import java.util.List;

public record ChatDto(
        Integer id,
        String name,
        String description,
        List<UserProfileDto> chatMembers
) {
    public static ChatDto fromEntity(Chat chat){
        return new ChatDto(
                chat.getId(),
                chat.getChatName(),
                chat.getChatDescription(),
                UserProfileDto.fromCollection(chat.getChatMembers())
        );
    }

    public static List<ChatDto> fromCollection(List<Chat> chats){
        return chats.stream().map(ChatDto::fromEntity).toList();
    }
}
