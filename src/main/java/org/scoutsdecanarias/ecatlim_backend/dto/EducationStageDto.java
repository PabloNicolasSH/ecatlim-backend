package org.scoutsdecanarias.ecatlim_backend.dto;

import org.scoutsdecanarias.ecatlim_backend.entity.EducationStage;

import java.util.List;
import java.util.stream.Collectors;

public record EducationStageDto(
        int id,
        String name,
        String code,
        String description,
        boolean previousStageRequired,
        Integer previousStageId
) {
    public static EducationStageDto fromEntity(EducationStage educationStage){
        return new EducationStageDto(
                educationStage.getId(),
                educationStage.getName(),
                educationStage.getCode(),
                educationStage.getDescription(),
                educationStage.isPreviousStageRequired(),
                educationStage.isPreviousStageRequired() ? educationStage.getPreviousStage().getId() : null
        );
    }

    public static List<EducationStageDto> fromCollection(List<EducationStage> educationStages){
        return educationStages.stream().map(EducationStageDto::fromEntity).collect(Collectors.toList());
    }
}
