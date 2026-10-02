package org.scoutsdecanarias.ecatlim_backend.features.user.dto;

public record UserMeFormDto(
        String name,
        String surname,
        String phone,
        String nif,
        Integer census,
        String address,
        String city,
        String country
) {
}
