package org.scoutsdecanarias.ecatlim_backend.features.user.dto;

import org.scoutsdecanarias.ecatlim_backend.features.education_stage.EducationStage;

public record TrainerStageDto(Integer id, String code, String name) {
    public static TrainerStageDto fromEntity(EducationStage stage) {
        return stage == null ? null : new TrainerStageDto(stage.getId(), stage.getCode(), stage.getName());
    }
}
