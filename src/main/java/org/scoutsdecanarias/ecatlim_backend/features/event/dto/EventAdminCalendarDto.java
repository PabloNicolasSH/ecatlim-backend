package org.scoutsdecanarias.ecatlim_backend.features.event.dto;

import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.dto.LessonBlockCalendarSummaryDto;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.SimpleUserDto;

import java.time.LocalDateTime;
import java.util.List;

public record EventAdminCalendarDto(
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
        SimpleUserDto director,
        List<EnrolledUserDto> students,
        String status,
        EventConfigDto eventConfig
) {
}
