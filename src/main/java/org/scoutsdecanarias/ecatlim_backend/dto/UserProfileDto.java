package org.scoutsdecanarias.ecatlim_backend.dto;

import org.scoutsdecanarias.ecatlim_backend.entity.User;

import java.util.List;
import java.util.stream.Collectors;

public record UserProfileDto(Integer id, String name, String surname, String email, String phone, Integer census, String role, String nif, String address, String city, String country, ScoutGroupDto scoutGroup) {

    public static UserProfileDto fromEntity(User user){
        return new UserProfileDto(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getEmail(),
                user.getPhone(),
                user.getCensus(),
                user.getRole().name(),
                user.getNif(),
                user.getAddress(),
                user.getCity(),
                user.getCountry(),
                ScoutGroupDto.fromEntity(user.getScoutGroup())
        );
    }

    public static List<UserProfileDto> fromCollection(List<User> users){
        return users.stream().map(UserProfileDto::fromEntity).collect(Collectors.toList());
    }
}
