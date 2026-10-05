package org.scoutsdecanarias.ecatlim_backend.features.recognition.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.scoutsdecanarias.ecatlim_backend.features.recognition.enums.RecognitionType;

public record RecognitionFormDto(
        @NotNull(message = "Debes indicar el tipo de convalidación")
        RecognitionType type,

        @Size(max = 2000, message = "El comentario no puede superar los 2000 caracteres")
        String comment
) {
}
