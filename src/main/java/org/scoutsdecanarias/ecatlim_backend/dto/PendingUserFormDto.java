package org.scoutsdecanarias.ecatlim_backend.dto;

public record PendingUserFormDto(
        String name,
        String surname,
        String email,
        Integer scoutGroupId
) {
}
