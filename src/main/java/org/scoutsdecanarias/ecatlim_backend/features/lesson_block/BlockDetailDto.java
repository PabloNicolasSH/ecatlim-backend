package org.scoutsdecanarias.ecatlim_backend.features.lesson_block;

import org.scoutsdecanarias.ecatlim_backend.features.activity.dto.ActivityDetailDto;

import java.util.Date;
import java.util.List;

public record BlockDetailDto(
        String code,
        String name,
        String status,
        Date completionDate,
        List<ActivityDetailDto> activities) {
}
