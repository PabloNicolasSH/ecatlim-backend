package org.scoutsdecanarias.ecatlim_backend.features.education_stage.dto;

import org.scoutsdecanarias.ecatlim_backend.features.module.ModuleDto;
import org.scoutsdecanarias.ecatlim_backend.features.education_stage.EducationStage;
import org.scoutsdecanarias.ecatlim_backend.features.module.Module;
import org.scoutsdecanarias.ecatlim_backend.features.module.ModuleType;

import java.util.List;
import java.util.stream.Collectors;

public record EducationStageDto(
        int id,
        String name,
        String code,
        String description,
        boolean previousStageRequired,
        Integer previousStageId,
        int onlineHours,
        int contactHours,
        int practicalHours,
        int allocatedOnlineHours,
        int allocatedContactHours,
        int allocatedPracticalHours,
        List<ModuleDto> modules
) {
    public static EducationStageDto fromEntity(EducationStage educationStage){
        int allocatedOnlineHours = 0;
        int allocatedContactHours = 0;
        int allocatedPracticalHours = 0;

        for (Module module : educationStage.getModules()) {
            if (module.getType() == ModuleType.THEORETICAL) {
                allocatedOnlineHours += module.getOnlineHours();
                allocatedContactHours += module.getContactHours();
            } else if (module.getType() == ModuleType.PRACTICAL) {
                allocatedPracticalHours += module.getOnlineHours() + module.getContactHours();
            }
        }

        return new EducationStageDto(
                educationStage.getId(),
                educationStage.getName(),
                educationStage.getCode(),
                educationStage.getDescription(),
                educationStage.isPreviousStageRequired(),
                educationStage.isPreviousStageRequired() ? educationStage.getPreviousStage().getId() : null,
                educationStage.getOnlineHours(),
                educationStage.getContactHours(),
                educationStage.getPracticalHours(),
                allocatedOnlineHours,
                allocatedContactHours,
                allocatedPracticalHours,
                ModuleDto.fromCollection(educationStage.getModules())
        );
    }

    public static List<EducationStageDto> fromCollection(List<EducationStage> educationStages){
        return educationStages.stream().map(EducationStageDto::fromEntity).collect(Collectors.toList());
    }
}
