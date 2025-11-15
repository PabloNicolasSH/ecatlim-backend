package org.scoutsdecanarias.ecatlim_backend.dto;

import org.scoutsdecanarias.ecatlim_backend.entity.User;

import java.util.List;

public record SimpleUserDto(
        Integer id,
        String name,
        String email
) {
    public static SimpleUserDto fromEntity(User user) {
        return new SimpleUserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public static List<SimpleUserDto> fromCollection(List<User> users) {
        return users.stream().map(SimpleUserDto::fromEntity).toList();
    }
}
