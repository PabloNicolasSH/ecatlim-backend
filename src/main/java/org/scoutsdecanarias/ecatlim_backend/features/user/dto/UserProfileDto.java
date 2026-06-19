package org.scoutsdecanarias.ecatlim_backend.features.user.dto;

import org.scoutsdecanarias.ecatlim_backend.features.scout_group.ScoutGroupDto;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;

import java.util.List;
import java.util.stream.Collectors;

public record UserProfileDto(
        Integer id,
        String name,
        String surname,
        String email,
        String phone,
        Integer census,
        String role,
        String nif,
        String address,
        String city,
        String country,
        ScoutGroupDto scoutGroup,
        String avatarUrl
) {

    public static UserProfileDto fromEntity(User user){
        boolean hasProfile = user.getProfile() != null;

        String avatarUrl = null;
        if (hasProfile && user.getProfile().getProfilePicture() != null) {
            avatarUrl = "/users/me/files/" + user.getProfile().getProfilePicture().getId();
        }

        return new UserProfileDto(
                user.getId(),
                hasProfile ? user.getProfile().getName() : "Administrador",
                hasProfile ? user.getProfile().getSurname() : "Global",
                user.getEmail(),
                hasProfile ? user.getProfile().getPhone() : null,
                hasProfile ? user.getProfile().getCensus() : 0,
                user.getRole().name(),
                hasProfile ? user.getProfile().getNif() : null,
                hasProfile ? user.getProfile().getAddress() : null,
                hasProfile ? user.getProfile().getCity() : null,
                hasProfile ? user.getProfile().getCountry() : null,
                (hasProfile && user.getProfile().getScoutGroup() != null)
                        ? ScoutGroupDto.fromEntity(user.getProfile().getScoutGroup())
                        : null,
                avatarUrl
        );
    }

    public static List<UserProfileDto> fromCollection(List<User> users){
        return users.stream().map(UserProfileDto::fromEntity).collect(Collectors.toList());
    }
}
