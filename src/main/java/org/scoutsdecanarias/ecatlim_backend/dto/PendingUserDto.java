package org.scoutsdecanarias.ecatlim_backend.dto;

import org.scoutsdecanarias.ecatlim_backend.entity.PendingUser;

import java.util.List;
import java.util.stream.Collectors;

public record PendingUserDto(
        String name,
        String surname,
        String email,
        String nif,
        ScoutGroupDto scoutGroup
) {
    public static PendingUserDto fromEntity(PendingUser pendingUser) {
        return new PendingUserDto(
                pendingUser.getName(),
                pendingUser.getSurname(),
                pendingUser.getEmail(),
                pendingUser.getNif(),
                ScoutGroupDto.fromEntity(pendingUser.getScoutGroup())
        );
    }

    public static List<PendingUserDto> fromCollection(List<PendingUser> pendingUsers) {
        return pendingUsers.stream().map(PendingUserDto::fromEntity).collect(Collectors.toList());
    }
}
