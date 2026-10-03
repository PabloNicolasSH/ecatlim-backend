package org.scoutsdecanarias.ecatlim_backend.features.module;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.LessonBlock;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.dto.LessonBlockDto;

import java.util.List;
import java.util.stream.Collectors;

public record ModuleDto(
        Integer id,

        @NotBlank(message = "El nombre del módulo es obligatorio")
        @Size(max = 255, message = "El nombre del módulo no puede superar los 255 caracteres")
        String name,

        Integer moduleId,

        @NotNull(message = "La descripción del módulo no puede ser nula")
        @Size(max = 1000, message = "La descripción del módulo no puede superar los 1000 caracteres")
        String description,

        @NotBlank(message = "El tipo de módulo es obligatorio")
        @Pattern(regexp = "Teórico|Práctico|THEORETICAL|PRACTICAL", message = "El tipo de módulo no es válido")
        String type,

        @Min(value = 0, message = "Las horas online no pueden ser negativas")
        @Max(value = 10000, message = "Las horas online no pueden superar 10000")
        int onlineHours,

        @Min(value = 0, message = "Las horas presenciales no pueden ser negativas")
        @Max(value = 10000, message = "Las horas presenciales no pueden superar 10000")
        int contactHours,

        @Min(value = 1, message = "Debes seleccionar la etapa formativa")
        int educationStage,
        int allocatedOnlineHours,
        int allocatedContactHours,
        List<LessonBlockDto> lessonBlocks
) {
    public static ModuleDto fromEntity(Module module) {
        int allocatedOnlineHours = 0;
        int allocatedContactHours = 0;

        for (LessonBlock lessonBlock : module.getLessonBlocks()){
            allocatedOnlineHours += lessonBlock.getOnlineHours();
            allocatedContactHours += lessonBlock.getContactHours();
        }

        return new ModuleDto(
                module.getId(),
                module.getName(),
                module.getModuleId(),
                module.getDescription(),
                module.getType().name(),
                module.getOnlineHours(),
                module.getContactHours(),
                module.getEducationStage().getId(),
                allocatedOnlineHours,
                allocatedContactHours,
                LessonBlockDto.fromCollections(module.getLessonBlocks())
        );
    }

    public static List<ModuleDto> fromCollection(List<Module> modules) {
        return modules.stream().map(ModuleDto::fromEntity).collect(Collectors.toList());
    }
}
