package org.scoutsdecanarias.ecatlim_backend.features.education_stage.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EducationStageFormDto(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 255, message = "El nombre no puede superar los 255 caracteres")
        String name,

        @NotBlank(message = "El código es obligatorio")
        @Size(max = 255, message = "El código no puede superar los 255 caracteres")
        String code,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
        String description,

        @Min(value = 0, message = "Las horas online no pueden ser negativas")
        @Max(value = 10000, message = "Las horas online no pueden superar 10000")
        int onlineHours,

        @Min(value = 0, message = "Las horas presenciales no pueden ser negativas")
        @Max(value = 10000, message = "Las horas presenciales no pueden superar 10000")
        int contactHours,

        @Min(value = 0, message = "Las horas prácticas no pueden ser negativas")
        @Max(value = 10000, message = "Las horas prácticas no pueden superar 10000")
        int practicalHours,

        Boolean previousStageRequired,
        int previousStageId
) {
}
