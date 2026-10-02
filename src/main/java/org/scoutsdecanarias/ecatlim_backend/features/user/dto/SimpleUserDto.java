package org.scoutsdecanarias.ecatlim_backend.features.user.dto;

import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;

import java.util.List;

public record SimpleUserDto(
        Integer id,
        String name,
        String surname,
        String email,
        String avatarUrl
) {
    public static SimpleUserDto fromEntity(User user) {

        boolean hasProfile  = user.getProfile() != null;
        boolean hasAvatar = hasProfile && user.getProfile().getProfilePicture() != null;

        return new SimpleUserDto(
                user.getId(),
                hasProfile ? user.getProfile().getName() : null,
                hasProfile ? user.getProfile().getSurname() : null,
                user.getEmail(),
                hasAvatar ? "/users/me/files/" + user.getProfile().getProfilePicture().getId() : null
        );
    }

    public static List<SimpleUserDto> fromCollection(List<User> activeUsers) {
        return activeUsers.stream().map(SimpleUserDto::fromEntity).toList();
    }
}
