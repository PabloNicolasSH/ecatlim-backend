package org.scoutsdecanarias.ecatlim_backend.features.event.dto;

import org.scoutsdecanarias.ecatlim_backend.dto.TimelineItemFormDto;
import org.scoutsdecanarias.ecatlim_backend.features.event.entity.Event;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.LessonBlock;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;

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
) {
    public static EventFormDto fromEntity(Event event) {
        return new EventFormDto(
                event.getId(),
                event.getTitle(),
                event.getShortname(),
                event.getDescription(),
                event.getContents(),
                event.getStartDate(),
                event.getEndDate(),
                event.getLocation(),
                event.getOrganizer(),
                event.getStatus() != null ? event.getStatus().toString() : null,
                event.getDirector() != null ? event.getDirector().getId() : null,
                event.getFacilitators() != null ?
                        event.getFacilitators().stream().map(User::getId).toList() : List.of(),
                event.getEducationStage() != null ? event.getEducationStage().getId() : null,
                event.getLessonBlocks() != null ?
                        event.getLessonBlocks().stream().map(LessonBlock::getId).toList() : List.of(),
                TimelineItemFormDto.fromCollection(event.getTimelineItems()),
                event.getEventConfiguration() != null ? EventConfigDto.fromEntity(event.getEventConfiguration()) : null
        );
    }
}
