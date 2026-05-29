package org.scoutsdecanarias.ecatlim_backend.dto;

import org.scoutsdecanarias.ecatlim_backend.entity.EducationSession;
import org.scoutsdecanarias.ecatlim_backend.entity.TimelineItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record TimelineItemFormDto(
        Integer id,
        String title,
        String description,
        LocalDateTime startTime,
        LocalDateTime endTime,
        TimelineItem.ItemType itemType,
        EducationSessionFormDto educationSession
) {
    public static TimelineItemFormDto fromEntity(TimelineItem item) {
        EducationSession session = item.getEducationSession();
        return new TimelineItemFormDto(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getStartTime(),
                item.getEndTime(),
                item.getItemType(),
                session != null ? EducationSessionFormDto.fromEntity(item.getEducationSession()) : null
        );
    }

    public static List<TimelineItemFormDto> fromCollection(List<TimelineItem> timelineItems){
        return timelineItems.stream().map(TimelineItemFormDto::fromEntity).collect(Collectors.toList());
    }

    public static TimelineItem fromDto(TimelineItemFormDto dto){
        TimelineItem timelineItem = new TimelineItem();
        timelineItem.setId(dto.id());
        timelineItem.setTitle(dto.title());
        timelineItem.setDescription(dto.description());
        timelineItem.setStartTime(dto.startTime());
        timelineItem.setEndTime(dto.endTime());
        timelineItem.setItemType(dto.itemType());
        return timelineItem;
    }

    public static List<TimelineItem> fromDtoCollection(List<TimelineItemFormDto> dtos){
        return dtos.stream().map(TimelineItemFormDto::fromDto).collect(Collectors.toList());
    }
}
