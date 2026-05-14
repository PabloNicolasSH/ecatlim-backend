package org.scoutsdecanarias.ecatlim_backend.dto.event;

import java.time.LocalDateTime;
import java.util.List;

public record EventUserCalendarDto(
        Integer id,
        String title,
        String description,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String location,
        String organizer,
        List<String> lessonBlockCodes,
        Integer attendeesCount,
        String educationStageCode,
        boolean isCurrentUserAttending,
        boolean canParticipate
) {
}
