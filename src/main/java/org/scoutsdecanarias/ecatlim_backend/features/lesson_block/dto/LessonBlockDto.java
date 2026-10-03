package org.scoutsdecanarias.ecatlim_backend.features.lesson_block.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.LessonBlock;

import java.util.List;
import java.util.stream.Collectors;

public record LessonBlockDto(
        int id,

        @NotBlank(message = "El nombre del bloque formativo es obligatorio")
        @Size(max = 255, message = "El nombre del bloque formativo no puede superar los 255 caracteres")
        String name,

        @Min(value = 1, message = "El número del bloque formativo debe ser al menos 1")
        int lessonBlockId,

        @NotNull(message = "La descripción del bloque formativo no puede ser nula")
        @Size(max = 1000, message = "La descripción del bloque formativo no puede superar los 1000 caracteres")
        String description,

        @NotNull(message = "Las horas online del bloque formativo son obligatorias")
        @Min(value = 0, message = "Las horas online no pueden ser negativas")
        @Max(value = 10000, message = "Las horas online no pueden superar 10000")
        Integer onlineHours,

        @NotNull(message = "Las horas presenciales del bloque formativo son obligatorias")
        @Min(value = 0, message = "Las horas presenciales no pueden ser negativas")
        @Max(value = 10000, message = "Las horas presenciales no pueden superar 10000")
        Integer contactHours,

        boolean recognizable,

        @NotNull(message = "Debes seleccionar el módulo")
        Integer moduleId,
        String code,
        Integer educationStageId
) {
    public static LessonBlockDto fromEntity(LessonBlock lessonBlock) {
        return new LessonBlockDto(
                lessonBlock.getId(),
                lessonBlock.getName(),
                lessonBlock.getLessonBlockId(),
                lessonBlock.getDescription(),
                lessonBlock.getOnlineHours(),
                lessonBlock.getContactHours(),
                lessonBlock.isRecognizable(),
                lessonBlock.getModule().getId(),
                lessonBlock.getCode(),
                lessonBlock.getEducationStageId()
        );
    }

    public static List<LessonBlockDto> fromCollections(List<LessonBlock> lessonBlocks) {
        return lessonBlocks.stream().map(LessonBlockDto::fromEntity).collect(Collectors.toList());
    }
}
