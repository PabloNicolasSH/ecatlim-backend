package org.scoutsdecanarias.ecatlim_backend.features.lesson_block;

import org.scoutsdecanarias.ecatlim_backend.features.activity.dto.ActivityDetailDto;
import org.scoutsdecanarias.ecatlim_backend.features.recognition.dto.RecognitionSummaryDto;

import java.util.Date;
import java.util.List;

public record BlockDetailDto(
        Integer id,
        String code,
        String name,
        String status,
        Date completionDate,
        boolean recognizable,
        RecognitionSummaryDto recognition,
        List<ActivityDetailDto> activities) {
}
