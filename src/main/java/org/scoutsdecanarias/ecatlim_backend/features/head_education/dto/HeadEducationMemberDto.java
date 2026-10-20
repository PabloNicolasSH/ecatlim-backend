package org.scoutsdecanarias.ecatlim_backend.features.head_education.dto;

import org.scoutsdecanarias.ecatlim_backend.features.user.dto.StudentOverviewDto;

public record HeadEducationMemberDto(
        Integer id,
        String name,
        String surname,
        String email,
        String avatarUrl,
        boolean enabled,
        StudentOverviewDto.CurrentStageDto currentStage
) {
}
