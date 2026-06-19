package org.scoutsdecanarias.ecatlim_backend.features.activity;

public record ActivityDetailDto(
        String name,
        String result,
        boolean completed
) {
}
