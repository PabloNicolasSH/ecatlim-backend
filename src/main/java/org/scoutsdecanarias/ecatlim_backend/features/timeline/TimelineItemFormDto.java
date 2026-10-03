package org.scoutsdecanarias.ecatlim_backend.features.timeline;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.scoutsdecanarias.ecatlim_backend.features.education_session.EducationSession;
import org.scoutsdecanarias.ecatlim_backend.features.education_session.EducationSessionFormDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record TimelineItemFormDto(
        Integer id,

        @NotBlank(message = "El título de cada elemento del cronograma es obligatorio")
        @Size(max = 255, message = "El título de un elemento del cronograma no puede superar los 255 caracteres")
        String title,

        @Size(max = 255, message = "La descripción de un elemento del cronograma no puede superar los 255 caracteres")
        String description,

        @NotNull(message = "La hora de inicio de cada elemento del cronograma es obligatoria")
        LocalDateTime startTime,

        @NotNull(message = "La hora de fin de cada elemento del cronograma es obligatoria")
        LocalDateTime endTime,

        @NotNull(message = "El tipo de cada elemento del cronograma es obligatorio")
        TimelineItem.ItemType itemType,

        EducationSessionFormDto educationSession
) {
    @JsonIgnore
    @AssertTrue(message = "En el cronograma, la hora de fin debe ser posterior a la de inicio")
    public boolean isEndTimeAfterStartTime() {
        return startTime == null || endTime == null || endTime.isAfter(startTime);
    }

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
