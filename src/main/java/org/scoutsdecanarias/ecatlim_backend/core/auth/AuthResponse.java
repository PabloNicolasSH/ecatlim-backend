package org.scoutsdecanarias.ecatlim_backend.core.auth;

import org.scoutsdecanarias.ecatlim_backend.features.user.dto.UserDto;

public record AuthResponse(
    String token,
    UserDto user
) {
}