package org.scoutsdecanarias.ecatlim_backend.dto;

import org.scoutsdecanarias.ecatlim_backend.entity.Event;

import java.time.LocalDateTime;
import java.util.List;

public record EventDto(
        Integer id,
        String title,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String location,
        String organizer,
        Integer attendeesCount,
        List<TimelineItemDto> timeline
) {
    public static EventDto fromEntity(Event event) {
        return new EventDto(
                event.getId(),
                event.getTitle(),
                event.getStartDate(),
                event.getEndDate(),
                event.getLocation(),
                event.getOrganizer(),
                event.getAttendees().size(),
                event.getTimelineItems().stream()
                        .map(TimelineItemDto::fromEntity)
                        .toList()
        );
    }
}
