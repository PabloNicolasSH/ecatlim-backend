package org.scoutsdecanarias.ecatlim_backend.dto;

import org.scoutsdecanarias.ecatlim_backend.entity.ScoutGroup;
import org.scoutsdecanarias.ecatlim_backend.entity.User;

public record UserProfileDto(String name, String surname, String email, String phone, Integer census, String nif, String Address, String city, String country, ScoutGroup scoutGroup) {

    public static UserProfileDto fromEntity(User user){
        return new UserProfileDto(
                user.getName(),
                user.getSurname(),
                user.getEmail(),
                user.getPhone(),
                user.getCensus(),
                user.getNif(),
                user.getAddress(),
                user.getCity(),
                user.getCountry(),
                user.getScoutGroup()
        );
    }
}
