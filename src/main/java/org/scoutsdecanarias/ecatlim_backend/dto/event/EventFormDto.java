package org.scoutsdecanarias.ecatlim_backend.dto.event;

import org.scoutsdecanarias.ecatlim_backend.dto.TimelineItemFormDto;

import java.time.LocalDateTime;
import java.util.List;

public record EventFormDto(
        Integer id,
        String title,
        String description,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String location,
        String organizer,
        Integer directorId,
        List<Integer> lessonBlockIds,
        List<TimelineItemFormDto> timelineItems
) {}
