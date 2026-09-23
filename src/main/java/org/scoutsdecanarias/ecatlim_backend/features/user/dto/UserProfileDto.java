package org.scoutsdecanarias.ecatlim_backend.features.user.dto;

import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.enums.Role;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record UserProfileDto(
        Integer id,
        String email,
        Set<String> role,
        ProfileDto profile
) {

    public static UserProfileDto fromEntity(User user){
        boolean hasProfile = user.getProfile() != null;

        return new UserProfileDto(
                user.getId(),
                user.getEmail(),
                user.getRoles().stream()
                        .map(Role::name)
                        .collect(Collectors.toSet()),
                hasProfile ? ProfileDto.fromEntity(user.getProfile()) : null
        );
    }

    public static List<UserProfileDto> fromCollection(List<User> users){
        return users.stream().map(UserProfileDto::fromEntity).collect(Collectors.toList());
    }
}
