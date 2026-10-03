package org.scoutsdecanarias.ecatlim_backend.features.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.scoutsdecanarias.ecatlim_backend.shared.utils.ValidIdDocument;
import org.scoutsdecanarias.ecatlim_backend.shared.utils.ValidationPatterns;

public record UserMeFormDto(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 255, message = "El nombre no puede superar los 255 caracteres")
        String name,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 255, message = "Los apellidos no pueden superar los 255 caracteres")
        String surname,

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
        String country
) {
}
