package org.scoutsdecanarias.ecatlim_backend.features.activity.dto;

import java.util.List;

public record QuestionCreationDto(
        String questionText,
        String responseType,
        List<OptionCreationDto> options
) {
}
