package org.scoutsdecanarias.ecatlim_backend.core.auth.password;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordDto(
        @NotBlank(message = "La contraseña actual es obligatoria") String currentPassword,
        @NotBlank(message = "La nueva contraseña es obligatoria") @ValidPassword String newPassword,
        @NotBlank(message = "Debes repetir la nueva contraseña") String newPasswordRepeat
) {}
