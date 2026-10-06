package org.scoutsdecanarias.ecatlim_backend.features.recognition.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.scoutsdecanarias.ecatlim_backend.features.recognition.enums.RecognitionDecision;

public record RecognitionResponseFormDto(
        @NotNull(message = "Debes indicar la respuesta")
        RecognitionDecision decision,

        @Size(max = 2000, message = "El comentario no puede superar los 2000 caracteres")
        String comment
) {
}
