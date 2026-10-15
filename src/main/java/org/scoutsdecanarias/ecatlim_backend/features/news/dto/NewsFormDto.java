package org.scoutsdecanarias.ecatlim_backend.features.news.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewsFormDto(
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 150, message = "El título no puede superar los 150 caracteres")
        String title,

        @NotBlank(message = "El texto es obligatorio")
        @Size(max = 5000, message = "El texto no puede superar los 5000 caracteres")
        String content,

        boolean removeImage
) {
}
