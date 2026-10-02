package org.scoutsdecanarias.ecatlim_backend.features.user.dto;

import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;

import java.util.Comparator;
import java.util.List;

public record TeamMemberDto(
        Integer id,
        String name,
        String surname,
        String email,
        String avatarUrl,
        String entityName,
        List<String> roles
) {
    public static TeamMemberDto fromEntity(User user) {
        SimpleUserDto simple = SimpleUserDto.fromEntity(user);
        boolean hasGroup = user.getProfile() != null && user.getProfile().getScoutGroup() != null;
        return new TeamMemberDto(
                user.getId(),
                simple.name(),
                simple.surname(),
                user.getEmail(),
                simple.avatarUrl(),
                hasGroup ? user.getProfile().getScoutGroup().getName() : null,
                user.getRoles().stream().map(Enum::name).sorted(Comparator.naturalOrder()).toList()
        );
    }
}
