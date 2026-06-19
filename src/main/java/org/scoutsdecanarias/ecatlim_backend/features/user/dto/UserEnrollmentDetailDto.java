package org.scoutsdecanarias.ecatlim_backend.features.user.dto;

import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.BlockDetailDto;

import java.util.List;

public record UserEnrollmentDetailDto(
        Integer id,
        String stageName,
        boolean completed,
        double percentage,
        List<BlockDetailDto> blocks
) {}
