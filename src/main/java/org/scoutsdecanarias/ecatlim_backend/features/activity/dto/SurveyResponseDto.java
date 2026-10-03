package org.scoutsdecanarias.ecatlim_backend.features.activity.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SurveyResponseDto(
        @NotNull(message = "Falta el identificador de la pregunta")
        Integer id,

        @Size(max = 255, message = "La respuesta no puede superar los 255 caracteres")
        String responseValue
) {
}
