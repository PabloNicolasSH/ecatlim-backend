package org.scoutsdecanarias.ecatlim_backend.event.dto;

import org.scoutsdecanarias.ecatlim_backend.dto.user.SimpleUserDto;
import org.scoutsdecanarias.ecatlim_backend.event.entity.EventConfiguration;
import org.scoutsdecanarias.ecatlim_backend.lesson_block.LessonBlockCalendarSummaryDto;

import javax.naming.InterruptedNamingException;
import java.time.LocalDateTime;
import java.util.Date;
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
