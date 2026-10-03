package org.scoutsdecanarias.ecatlim_backend.features.event.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.scoutsdecanarias.ecatlim_backend.features.event.entity.Event;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.LessonBlock;
import org.scoutsdecanarias.ecatlim_backend.features.timeline.TimelineItemFormDto;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public record EventFormDto(
        Integer id,

        @NotBlank(message = "El título es obligatorio")
        @Size(max = 255, message = "El título no puede superar los 255 caracteres")
        String title,

        @NotBlank(message = "El nombre corto es obligatorio")
        @Size(max = 255, message = "El nombre corto no puede superar los 255 caracteres")
        String shortname,

        @NotNull(message = "La descripción no puede ser nula")
        @Size(max = 10000, message = "La descripción no puede superar los 10000 caracteres")
        String description,

        @NotBlank(message = "Los contenidos son obligatorios")
        @Size(max = 10000, message = "Los contenidos no pueden superar los 10000 caracteres")
        String contents,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDateTime startDate,

        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDateTime endDate,

        @NotBlank(message = "El lugar es obligatorio")
        @Size(max = 255, message = "El lugar no puede superar los 255 caracteres")
        String location,

        @NotBlank(message = "El organizador es obligatorio")
        @Size(max = 255, message = "El organizador no puede superar los 255 caracteres")
        String organizer,

        @NotBlank(message = "El estado es obligatorio")
        @Pattern(regexp = "PUBLISHED|PENDING|DRAFT", message = "El estado del evento no es válido")
        String status,

        @NotNull(message = "Debes indicar el director del evento")
        Integer directorId,

        @NotEmpty(message = "Debes seleccionar al menos un formador")
        List<@NotNull Integer> facilitatorIds,

        @NotNull(message = "Debes indicar la etapa formativa")
        Integer educationStageId,

        @NotEmpty(message = "Debes seleccionar al menos un bloque formativo")
        List<@NotNull Integer> lessonBlockIds,

        @Size(max = 500, message = "El cronograma no puede tener más de 500 elementos")
        List<@Valid @NotNull TimelineItemFormDto> timelineItems,

        @Valid
        @NotNull(message = "La configuración de inscripción es obligatoria")
        EventConfigDto eventConfiguration
) {
    @JsonIgnore
    @AssertTrue(message = "La fecha de fin debe ser posterior a la de inicio")
    public boolean isEndDateAfterStartDate() {
        return startDate == null || endDate == null || endDate.isAfter(startDate);
    }

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
