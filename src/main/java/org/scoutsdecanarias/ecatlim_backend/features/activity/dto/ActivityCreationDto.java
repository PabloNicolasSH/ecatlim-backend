package org.scoutsdecanarias.ecatlim_backend.features.activity.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ActivityCreationDto(
        String title,
        String description,
        String activityType,
        String evaluationMethod,
        LocalDateTime availableAt,
        LocalDateTime dueDate,
        Boolean isOptional,
        Integer lessonBlockId,
        Boolean isGradable,
        Integer maxAttempts,
        Double passingScore,
        List<QuestionCreationDto> questions
) {
}
