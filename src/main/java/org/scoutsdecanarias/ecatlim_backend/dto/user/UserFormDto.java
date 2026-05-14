package org.scoutsdecanarias.ecatlim_backend.dto.user;

import org.scoutsdecanarias.ecatlim_backend.enums.Role;

public record UserFormDto(
        String name,
        String surname,
        String email,
        String phone,
        String nif,
        Integer census,
        String address,
        String city,
        String country,
        Role role,
        Integer scoutGroupId) {
}
