package org.scoutsdecanarias.ecatlim_backend.features.certificate.dto;

import jakarta.validation.constraints.NotNull;

public record BlockPassedFormDto(
        @NotNull(message = "Debes indicar si el bloque está superado")
        Boolean passed
) {
}
