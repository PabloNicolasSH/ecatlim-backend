package org.scoutsdecanarias.ecatlim_backend.auth.password;

import jakarta.validation.constraints.NotNull;

public record ResetPasswordDto(
        @NotNull String token,
        @ValidPassword @NotNull String newPassword,
        @NotNull String newPasswordRepeat
) {}
