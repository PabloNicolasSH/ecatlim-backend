package org.scoutsdecanarias.ecatlim_backend.features.activity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ForumPublicationFormDto(
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 255, message = "El título no puede superar los 255 caracteres")
        String title,

        @NotBlank(message = "El contenido es obligatorio")
        @Size(max = 10000, message = "El contenido no puede superar los 10000 caracteres")
        String body
) {
}
