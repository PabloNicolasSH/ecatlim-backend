package org.scoutsdecanarias.ecatlim_backend.dto;

import org.scoutsdecanarias.ecatlim_backend.entity.EducationSession;
import org.scoutsdecanarias.ecatlim_backend.entity.TimelineItem;
import org.scoutsdecanarias.ecatlim_backend.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record TimelineItemDto(
        Integer id,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String type,
        String lessonBlockTitle,
        String trainerName
) {
    public static TimelineItemDto fromEntity(TimelineItem item) {
        EducationSession session = item.getEducationSession();
        return new TimelineItemDto(
                item.getId(),
                item.getStartTime(),
                item.getEndTime(),
                session != null ? "FORMATIVE" : "BREAK",
                session != null && session.getLessonBlock() != null ? session.getLessonBlock().getName() : "Descanso/Comida",
                session != null && session.getEducator() != null ? session.getEducator().getName() : null
        );
    }

    public static List<TimelineItemDto> fromCollection(List<TimelineItem> timelineItems){
        return timelineItems.stream().map(TimelineItemDto::fromEntity).collect(Collectors.toList());
    }

    public static TimelineItem fromDto(TimelineItemDto dto){
        TimelineItem timelineItem = new TimelineItem();
        timelineItem.setId(dto.id());
        timelineItem.setStartTime(dto.startTime());
        timelineItem.setEndTime(dto.endTime());
        timelineItem.setTitle(dto.type());
        return timelineItem;
    }

    public static List<TimelineItem> fromDtoCollection(List<TimelineItemDto> dtos){
        return dtos.stream().map(TimelineItemDto::fromDto).collect(Collectors.toList());
    }
}
