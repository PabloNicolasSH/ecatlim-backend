package org.scoutsdecanarias.ecatlim_backend.features.event.dto;

import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;

import java.util.List;
import java.util.Set;

public record EnrolledUserDto(
        String name,
        String email,
        String paymentState
) {
    public static EnrolledUserDto fromEntity(User user) {
        return new EnrolledUserDto(
                user.getName(),
                user.getEmail(),
                "PAGADO"
        );
    }

    public static List<EnrolledUserDto> fromCollection(Set<User> attendees) {
        return attendees.stream().map(EnrolledUserDto::fromEntity).toList();
    }
}
