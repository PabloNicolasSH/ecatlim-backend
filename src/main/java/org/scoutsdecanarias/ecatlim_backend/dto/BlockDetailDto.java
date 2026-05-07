package org.scoutsdecanarias.ecatlim_backend.dto;

import java.util.Date;
import java.util.List;

public record BlockDetailDto(
        String id,
        String name,
        String status,
        Date date,
        List<ActivityDetailDto> activities) {
}
