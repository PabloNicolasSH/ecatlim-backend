package org.scoutsdecanarias.ecatlim_backend.features.module;

import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.LessonBlock;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.dto.LessonBlockDto;

import java.util.List;
import java.util.stream.Collectors;

public record ModuleDto(
        Integer id,
        String name,
        String description,
        String type,
        int onlineHours,
        int contactHours,
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
