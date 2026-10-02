package org.scoutsdecanarias.ecatlim_backend.features.event.dto;

import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.dto.LessonBlockCalendarSummaryDto;

import java.time.LocalDateTime;
import java.util.List;

public record AttendedEventDto(
        Integer id,
        String title,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String location,
        List<LessonBlockCalendarSummaryDto> lessonBlocks
) {
}
