package org.scoutsdecanarias.ecatlim_backend.features.module;

import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.BlockDetailDto;

import java.util.List;

public record ModuleDetailDto(
        String name,
        String code,
        List<BlockDetailDto> blocks
) {
}
