package org.scoutsdecanarias.ecatlim_backend.dto.user;

import org.scoutsdecanarias.ecatlim_backend.entity.User;

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
}
