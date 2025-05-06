package org.scoutsdecanarias.ecatlim_backend.dto;

public record UserMeFormDto(
        String name,
        String surname,
        String email,
        String phone,
        String nif,
        Integer census,
        String address,
        String city,
        String country
) {
}
