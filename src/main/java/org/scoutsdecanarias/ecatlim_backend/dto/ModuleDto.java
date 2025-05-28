package org.scoutsdecanarias.ecatlim_backend.dto;

import org.scoutsdecanarias.ecatlim_backend.entity.Module;

import java.util.List;
import java.util.stream.Collectors;

public record ModuleDto(
        String name,
        String description,
        String type,
        int onlineHours,
        int contactHours,
        int educationStage
) {
    public static ModuleDto fromEntity(Module module) {
        return new ModuleDto(
                module.getName(),
                module.getDescription(),
                module.getType().name(),
                module.getOnlineHours(),
                module.getContactHours(),
                module.getEducationStage().getId()
        );
    }

    public static List<ModuleDto> fromCollection(List<Module> modules) {
        return modules.stream().map(ModuleDto::fromEntity).collect(Collectors.toList());
    }
}
