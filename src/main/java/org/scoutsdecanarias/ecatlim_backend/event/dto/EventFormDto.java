package org.scoutsdecanarias.ecatlim_backend.event.dto;

import org.scoutsdecanarias.ecatlim_backend.dto.TimelineItemFormDto;
import org.scoutsdecanarias.ecatlim_backend.entity.LessonBlock;

import java.time.LocalDateTime;
import java.util.List;

public record EventFormDto(
        Integer id,
        String title,
        String shortname,
        String description,
        String contents,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String location,
        String organizer,
        String status,
        Integer directorId,
        List<Integer> facilitatorIds,
        Integer educationStageId,
        List<Integer> lessonBlockIds,
        List<TimelineItemFormDto> timelineItems,
        EventConfigDto eventConfiguration
) {}
