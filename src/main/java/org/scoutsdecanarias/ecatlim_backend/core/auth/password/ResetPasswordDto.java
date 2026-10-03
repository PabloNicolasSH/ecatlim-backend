package org.scoutsdecanarias.ecatlim_backend.core.auth.password;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordDto(
        @NotBlank(message = "El enlace de recuperación no es válido")
        @Size(max = 64, message = "El enlace de recuperación no es válido")
        String token,
        @NotBlank(message = "La nueva contraseña es obligatoria") @ValidPassword String newPassword,
        @NotBlank(message = "Debes repetir la nueva contraseña") String newPasswordRepeat
) {}
