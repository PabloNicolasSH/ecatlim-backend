package org.scoutsdecanarias.ecatlim_backend.features.user.dto;

import java.util.List;

public record StudentOverviewDto(
        List<StudentDto> students,
        List<StageCountDto> stages,
        int withoutStage
) {
    public record StudentDto(
            Integer id,
            String name,
            String surname,
            String email,
            String avatarUrl,
            String entityName,
            CurrentStageDto currentStage
    ) {
    }

    public record CurrentStageDto(Integer id, String name, String code, String status) {
    }

    public record StageCountDto(Integer stageId, String name, String code, int count) {
    }
}
