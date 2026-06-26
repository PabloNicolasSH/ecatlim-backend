package org.scoutsdecanarias.ecatlim_backend.features.user.dto;

import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;

import java.util.List;
import java.util.stream.Collectors;

public record UserProfileDto(
        Integer id,
        String email,
        String role,
        ProfileDto profile
) {

    public static UserProfileDto fromEntity(User user){
        boolean hasProfile = user.getProfile() != null;

        return new UserProfileDto(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                hasProfile ? ProfileDto.fromEntity(user.getProfile()) : null
        );
    }

    public static List<UserProfileDto> fromCollection(List<User> users){
        return users.stream().map(UserProfileDto::fromEntity).collect(Collectors.toList());
    }
}
