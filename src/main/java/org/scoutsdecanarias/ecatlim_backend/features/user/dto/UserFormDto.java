package org.scoutsdecanarias.ecatlim_backend.features.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.scoutsdecanarias.ecatlim_backend.features.user.enums.Role;
import org.scoutsdecanarias.ecatlim_backend.shared.utils.ValidIdDocument;
import org.scoutsdecanarias.ecatlim_backend.shared.utils.ValidationPatterns;

import java.util.Set;

public record UserFormDto(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 255, message = "El nombre no puede superar los 255 caracteres")
        String name,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 255, message = "Los apellidos no pueden superar los 255 caracteres")
        String surname,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        @Size(max = 255, message = "El email no puede superar los 255 caracteres")
        String email,

        @Pattern(regexp = "^$|" + ValidationPatterns.PHONE, message = ValidationPatterns.PHONE_MESSAGE)
        String phone,

        @ValidIdDocument
        String nif,

        @Min(value = 0, message = "El número de censo no puede ser negativo")
        @Max(value = 999999999, message = "El número de censo no es válido")
        Integer census,

        @Size(max = 255, message = "La dirección no puede superar los 255 caracteres")
        String address,

        @Size(max = 255, message = "La ciudad no puede superar los 255 caracteres")
        String city,

        @Size(max = 255, message = "El país no puede superar los 255 caracteres")
        String country,

        Set<Role> roles,
        Integer scoutGroupId) {
}
