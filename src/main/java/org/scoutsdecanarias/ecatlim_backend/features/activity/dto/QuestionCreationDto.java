package org.scoutsdecanarias.ecatlim_backend.features.activity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record QuestionCreationDto(
        @NotBlank(message = "El enunciado de la pregunta es obligatorio")
        @Size(max = 255, message = "El enunciado de la pregunta no puede superar los 255 caracteres")
        String questionText,

        @NotBlank(message = "El tipo de respuesta es obligatorio")
        @Pattern(regexp = "TEXT|SELECTION|VALUE", message = "El tipo de respuesta no es válido")
        String responseType,

        @Size(max = 20, message = "Una pregunta no puede tener más de 20 opciones")
        List<@Valid @NotNull OptionCreationDto> options
) {
}
