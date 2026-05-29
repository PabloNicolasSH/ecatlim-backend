package org.scoutsdecanarias.ecatlim_backend.dto.education_stage;

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
