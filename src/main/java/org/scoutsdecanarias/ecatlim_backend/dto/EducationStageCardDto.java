package org.scoutsdecanarias.ecatlim_backend.dto;

public record EducationStageCardDto(
        Integer id,
        String name,
        String description,
        String type,
        String status,
        boolean isEnabled
) {}
