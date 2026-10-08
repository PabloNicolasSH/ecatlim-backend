package org.scoutsdecanarias.ecatlim_backend.features.event.dto;

import jakarta.validation.constraints.NotNull;
import org.scoutsdecanarias.ecatlim_backend.features.event.enums.AttendanceType;

public record AttendanceFormDto(
        @NotNull(message = "Debes indicar la persona")
        Integer userId,

        @NotNull(message = "Debes indicar el bloque formativo")
        Integer lessonBlockId,

        AttendanceType attendance
) {
}
