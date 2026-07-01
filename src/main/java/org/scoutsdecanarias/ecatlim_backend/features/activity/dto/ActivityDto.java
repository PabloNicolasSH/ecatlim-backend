package org.scoutsdecanarias.ecatlim_backend.features.activity.dto;

import org.scoutsdecanarias.ecatlim_backend.features.activity.entity.Activity;

import java.time.LocalDateTime;
import java.util.List;

public record ActivityDto(
        Integer id,
        String title,
        String description,
        String activityType,
        String evaluationMethod,
        LocalDateTime availableAt,
        LocalDateTime dueDate,
        Boolean isOptional) {
    public static ActivityDto fromEntity(Activity activity) {
        return new ActivityDto(
                activity.getId(),
                activity.getTitle(),
                activity.getDescription(),
                activity.getActivityType().name(),
                activity.getEvaluationMethod().name(),
                activity.getAvailableAt(),
                activity.getDueDate(),
                activity.getIsOptional()
        );
    }

    public static List<ActivityDto> fromCollection(List<Activity> activitiesByEvent) {
        return activitiesByEvent.stream().map(ActivityDto::fromEntity).toList();
    }
}
