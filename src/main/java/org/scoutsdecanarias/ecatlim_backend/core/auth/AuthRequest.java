package org.scoutsdecanarias.ecatlim_backend.core.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AuthRequest {
    @NotBlank(message = "El email es obligatorio")
    @Size(max = 255, message = "El email no puede superar los 255 caracteres")
    private String username;

    @ToString.Exclude
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(max = 256, message = "La contraseña no puede superar los 256 caracteres")
    private String password;
}
