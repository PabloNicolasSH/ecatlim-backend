package org.scoutsdecanarias.ecatlim_backend.features.user.dto;

public record PendingUserFormDto(
        String name,
        String surname,
        String email,
        String nif,
        Integer scoutGroupId
) {
}
