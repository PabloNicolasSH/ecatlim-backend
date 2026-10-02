package org.scoutsdecanarias.ecatlim_backend.features.user.dto;

import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.enums.Role;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record UserDto(
        Integer id,
        String email,
        Set<String> roles,
        ProfileDto profile
) {

    public static UserDto fromEntity(User user){
        boolean hasProfile = user.getProfile() != null;

        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getRoles().stream()
                        .map(Role::name)
                        .collect(Collectors.toSet()),
                hasProfile ? ProfileDto.fromEntity(user.getProfile()) : null
        );
    }

    public static List<UserDto> fromCollection(List<User> users){
        return users.stream().map(UserDto::fromEntity).collect(Collectors.toList());
    }
}
