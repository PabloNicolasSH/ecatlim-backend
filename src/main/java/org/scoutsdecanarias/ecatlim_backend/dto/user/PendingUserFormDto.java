package org.scoutsdecanarias.ecatlim_backend.dto.user;

public record PendingUserFormDto(
        String name,
        String surname,
        String email,
        String nif,
        Integer scoutGroupId
) {
}
