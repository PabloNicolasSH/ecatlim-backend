package org.scoutsdecanarias.ecatlim_backend.event.dto;

import java.time.LocalDateTime;

public record EventHomeWidgetDto(
        Integer id,
        String title,
        LocalDateTime startDate,
        String location,
        String educationStageCode,
        Boolean isCurrentUserAttending,
        Boolean canParticipate
) {
}
