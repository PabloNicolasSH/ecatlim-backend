package org.scoutsdecanarias.ecatlim_backend.features.activity.dto;

public record ActivityDetailDto(
        String name,
        String result,
        boolean completed
) {
}
