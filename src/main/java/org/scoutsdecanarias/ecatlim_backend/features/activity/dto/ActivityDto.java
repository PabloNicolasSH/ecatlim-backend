package org.scoutsdecanarias.ecatlim_backend.features.activity.dto;

import org.scoutsdecanarias.ecatlim_backend.features.activity.entity.Activity;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.SimpleUserDto;

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
        Boolean isOptional,
        SimpleUserDto responsible,
        String progressStatus) {
    public static ActivityDto fromEntity(Activity activity) {
        return new ActivityDto(
                activity.getId(),
                activity.getTitle(),
                activity.getDescription(),
                activity.getActivityType().name(),
                activity.getEvaluationMethod().name(),
                activity.getAvailableAt(),
                activity.getDueDate(),
                activity.getIsOptional(),
                activity.getCorrectors().stream().findFirst().map(SimpleUserDto::fromEntity).orElse(null),
                null
        );
    }

    public ActivityDto withProgressStatus(String status) {
        return new ActivityDto(id, title, description, activityType, evaluationMethod, availableAt, dueDate, isOptional,
                responsible, status);
    }

    public static List<ActivityDto> fromCollection(List<Activity> activitiesByEvent) {
        return activitiesByEvent.stream().map(ActivityDto::fromEntity).toList();
    }
}
