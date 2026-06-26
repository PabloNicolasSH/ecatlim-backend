package org.scoutsdecanarias.ecatlim_backend.features.user.dto;

import org.scoutsdecanarias.ecatlim_backend.features.scout_group.ScoutGroupDto;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.UserProfile;

import java.util.List;
import java.util.stream.Collectors;

public record ProfileDto(
        String name,
        String surname,
        String phone,
        Integer census,
        String nif,
        String address,
        String city,
        String country,
        ScoutGroupDto scoutGroup,
        String avatarUrl
) {

    public static ProfileDto fromEntity(UserProfile profile) {
        String avatarUrl = null;
        if (profile.getProfilePicture() != null) {
            avatarUrl = "/users/me/files/" + profile.getProfilePicture().getId();
        }

        return new ProfileDto(
                profile.getName(),
                profile.getSurname(),
                profile.getPhone(),
                profile.getCensus(),
                profile.getNif(),
                profile.getAddress(),
                profile.getCity(),
                profile.getCountry(),
                (profile.getScoutGroup() != null)
                        ? ScoutGroupDto.fromEntity(profile.getScoutGroup())
                        : null,
                avatarUrl
        );
    }
}
