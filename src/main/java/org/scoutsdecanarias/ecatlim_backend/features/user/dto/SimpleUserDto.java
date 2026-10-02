package org.scoutsdecanarias.ecatlim_backend.features.user.dto;

import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;

public record SimpleUserDto(
        Integer id,
        String name,
        String email
) {
    public static SimpleUserDto fromEntity(User user) {

        boolean hasProfile  = user.getProfile() != null;

        return new SimpleUserDto(
                user.getId(),
                hasProfile ? user.getProfile().getName() : null,
                user.getEmail()
        );
    }
}
