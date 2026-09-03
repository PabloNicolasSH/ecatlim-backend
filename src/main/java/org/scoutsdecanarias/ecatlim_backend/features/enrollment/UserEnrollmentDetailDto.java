package org.scoutsdecanarias.ecatlim_backend.features.enrollment;

import org.scoutsdecanarias.ecatlim_backend.features.module.ModuleDetailDto;

import java.util.List;

public record UserEnrollmentDetailDto(
        Integer id,
        String stageName,
        boolean completed,
        double percentage,
        List<ModuleDetailDto> modules
) {}
