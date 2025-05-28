package org.scoutsdecanarias.ecatlim_backend.dto;

public record EducationStageFormDto(
        String name,
        String code,
        String description,
        Boolean previousStageRequired,
        int previousStageId
) {
}
