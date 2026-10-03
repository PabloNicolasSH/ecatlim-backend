package org.scoutsdecanarias.ecatlim_backend.features.activity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OptionCreationDto(
        @NotBlank(message = "El texto de la opción es obligatorio")
        @Size(max = 255, message = "El texto de la opción no puede superar los 255 caracteres")
        String optionText,

        Boolean isCorrect
) {
}
