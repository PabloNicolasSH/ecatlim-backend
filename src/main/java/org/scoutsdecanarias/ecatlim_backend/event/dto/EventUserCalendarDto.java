package org.scoutsdecanarias.ecatlim_backend.event.dto;

import org.scoutsdecanarias.ecatlim_backend.lesson_block.LessonBlockCalendarSummaryDto;

import java.time.LocalDateTime;
import java.util.List;

public record EventUserCalendarDto(
        Integer id,
        String title,
        String shortName,
        String description,
        String contents,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String location,
        String organizer,
        List<LessonBlockCalendarSummaryDto> lessonBlocks,
        Integer attendeesCount,
        String educationStageCode,
        boolean isCurrentUserAttending,
        boolean canParticipate,
        boolean isEventClosed,
        String status
) {
}
