package org.scoutsdecanarias.ecatlim_backend.features.lesson_block;

import org.scoutsdecanarias.ecatlim_backend.features.activity.ActivityDetailDto;

import java.util.Date;
import java.util.List;

public record BlockDetailDto(
        String id,
        String name,
        String status,
        Date date,
        List<ActivityDetailDto> activities) {
}
