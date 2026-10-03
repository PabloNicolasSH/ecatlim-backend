package org.scoutsdecanarias.ecatlim_backend.features.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import org.scoutsdecanarias.ecatlim_backend.shared.utils.ValidIdDocument;

public record PendingUserFormDto(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 255, message = "El nombre no puede superar los 255 caracteres")
        String name,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 255, message = "Los apellidos no pueden superar los 255 caracteres")
        String surname,

        @NotBlank(message = "El email es obligatorio", groups = {Default.class, ByEmail.class})
        @Email(message = "El email no tiene un formato válido", groups = {Default.class, ByEmail.class})
        @Size(max = 255, message = "El email no puede superar los 255 caracteres", groups = {Default.class, ByEmail.class})
        String email,

        @NotBlank(message = "El DNI, NIE o pasaporte es obligatorio")
        @ValidIdDocument
        String nif,

        Integer scoutGroupId
) {
    public interface ByEmail {}
}
