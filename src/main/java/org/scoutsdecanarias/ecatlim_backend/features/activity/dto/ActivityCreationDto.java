package org.scoutsdecanarias.ecatlim_backend.features.activity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

public record ActivityCreationDto(
        @NotBlank(message = "El título es obligatorio")
        @Size(min = 5, max = 255, message = "El título debe tener entre 5 y 255 caracteres")
        String title,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
        String description,

        @NotBlank(message = "El tipo de actividad es obligatorio")
        @Pattern(regexp = "FORUM|GLOSSARY|FILE_UPLOAD|SURVEY", message = "El tipo de actividad no es válido")
        String activityType,

        @NotBlank(message = "El método de evaluación es obligatorio")
        @Pattern(regexp = "AUTOMATIC|MANUAL", message = "El método de evaluación no es válido")
        String evaluationMethod,

        @NotNull(message = "La fecha de apertura es obligatoria")
        LocalDateTime availableAt,

        @NotNull(message = "La fecha de entrega es obligatoria")
        LocalDateTime dueDate,

        Boolean isOptional,

        @NotNull(message = "Debes indicar el bloque formativo")
        Integer lessonBlockId,

        Boolean isGradable,

        @Min(value = 1, message = "El número máximo de intentos debe ser al menos 1")
        @Max(value = 100, message = "El número máximo de intentos no puede superar 100")
        Integer maxAttempts,

        @DecimalMin(value = "0", message = "La nota mínima no puede ser negativa")
        @DecimalMax(value = "10", message = "La nota mínima no puede ser superior a 10")
        Double passingScore,

        @Size(max = 100, message = "Una encuesta no puede tener más de 100 preguntas")
        List<@Valid @NotNull QuestionCreationDto> questions,

        @NotNull(message = "Debes indicar el formador responsable de la actividad")
        Integer responsibleId
) {
    @AssertTrue(message = "La fecha de entrega debe ser posterior a la de apertura")
    public boolean isDueDateAfterAvailableAt() {
        return availableAt == null || dueDate == null || dueDate.isAfter(availableAt);
    }
}
