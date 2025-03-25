package org.scoutsdecanarias.ecatlim_backend.auth.password;

import jakarta.validation.constraints.NotNull;

public record ForgotPasswordDto(
        @NotNull String token,
        @ValidPassword @NotNull String newPassword,
        @NotNull String newPasswordRepeat
) {}
