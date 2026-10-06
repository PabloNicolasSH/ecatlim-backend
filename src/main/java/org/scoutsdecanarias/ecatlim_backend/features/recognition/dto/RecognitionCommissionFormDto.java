package org.scoutsdecanarias.ecatlim_backend.features.recognition.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RecognitionCommissionFormDto(
        @NotEmpty(message = "Debes elegir al menos una persona para la comisión")
        List<@NotNull Integer> userIds
) {
}
