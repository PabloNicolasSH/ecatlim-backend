package org.scoutsdecanarias.ecatlim_backend.dto;

import java.util.List;

public record UserEnrollmentDetailDto(
        Integer id,
        String stageName,
        boolean completed,
        double percentage,
        List<BlockDetailDto> blocks
) {}
