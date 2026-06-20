package org.scoutsdecanarias.ecatlim_backend.features.activity.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ActivityCreationDto(
        String activityType,
        String evaluationMethod,
        LocalDateTime availableAt,
        LocalDateTime dueDate,
        Boolean isGradable,
        Integer maxAttempts,
        Double passingScore,
        List<QuestionCreationDto> questions
) {
}
