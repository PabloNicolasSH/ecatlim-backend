package org.scoutsdecanarias.ecatlim_backend.features.education_stage.dto;

public record EducationStageFormDto(
        String name,
        String code,
        String description,
        int onlineHours,
        int contactHours,
        int practicalHours,
        Boolean previousStageRequired,
        int previousStageId
) {
}
