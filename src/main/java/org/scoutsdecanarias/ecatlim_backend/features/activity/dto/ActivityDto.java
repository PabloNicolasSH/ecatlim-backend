package org.scoutsdecanarias.ecatlim_backend.features.activity.dto;

import org.hibernate.Hibernate;
import org.scoutsdecanarias.ecatlim_backend.features.activity.entity.Activity;
import org.scoutsdecanarias.ecatlim_backend.features.activity.entity.SurveyActivity;
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
        String progressStatus,
        List<SurveyQuestionDto> questions) {
    public static ActivityDto fromEntity(Activity activity) {
        Object real = Hibernate.unproxy(activity);
        List<SurveyQuestionDto> questions = real instanceof SurveyActivity survey
                ? SurveyQuestionDto.fromCollection(survey.getSurveyQuestions())
                : null;
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
                null,
                questions
        );
    }

    public ActivityDto withProgressStatus(String status) {
        return new ActivityDto(id, title, description, activityType, evaluationMethod, availableAt, dueDate, isOptional,
                responsible, status, questions);
    }

    public static List<ActivityDto> fromCollection(List<Activity> activitiesByEvent) {
        return activitiesByEvent.stream().map(ActivityDto::fromEntity).toList();
    }
}
