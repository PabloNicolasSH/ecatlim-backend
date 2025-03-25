package org.scoutsdecanarias.ecatlim_backend.auth.password;

public record ChangePasswordDto(
        String currentPassword,
        @ValidPassword String newPassword,
        String newPasswordRepeat
) {}
