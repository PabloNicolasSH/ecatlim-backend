package org.scoutsdecanarias.ecatlim_backend.features.certificate.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SendBlockCertificatesFormDto(
        @NotEmpty(message = "Debes seleccionar al menos un bloque formativo")
        List<@NotNull Integer> lessonBlockIds
) {
}
