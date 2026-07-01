package org.scoutsdecanarias.ecatlim_backend.features.activity.dto;

public record OptionCreationDto(
        String optionText,
        Boolean isCorrect
) {
}
