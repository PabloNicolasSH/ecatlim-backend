package org.scoutsdecanarias.ecatlim_backend.features.activity.dto;

import org.scoutsdecanarias.ecatlim_backend.features.activity.entity.SurveyOption;
import org.scoutsdecanarias.ecatlim_backend.features.activity.entity.SurveyQuestion;

import java.util.Comparator;
import java.util.List;

public record SurveyQuestionDto(
        Integer id,
        String questionText,
        String responseType,
        List<OptionDto> options
) {
    public record OptionDto(Integer id, String optionText) {
        static OptionDto fromEntity(SurveyOption option) {
            return new OptionDto(option.getId(), option.getOptionText());
        }
    }

    public static SurveyQuestionDto fromEntity(SurveyQuestion question) {
        List<OptionDto> options = question.getOptions() == null ? List.of() : question.getOptions().stream()
                .sorted(Comparator.comparing(SurveyOption::getId))
                .map(OptionDto::fromEntity)
                .toList();
        return new SurveyQuestionDto(question.getId(), question.getQuestionText(), question.getResponseType().name(), options);
    }

    public static List<SurveyQuestionDto> fromCollection(List<SurveyQuestion> questions) {
        return questions == null ? List.of() : questions.stream()
                .sorted(Comparator.comparing(SurveyQuestion::getId))
                .map(SurveyQuestionDto::fromEntity)
                .toList();
    }
}
