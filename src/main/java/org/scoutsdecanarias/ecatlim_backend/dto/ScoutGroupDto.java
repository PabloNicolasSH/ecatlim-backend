package org.scoutsdecanarias.ecatlim_backend.dto;

import org.scoutsdecanarias.ecatlim_backend.entity.ScoutGroup;

public record ScoutGroupDto(String name, int provinceId, int groupNumber) {

    public static ScoutGroupDto fromEntity(ScoutGroup scoutGroup) {
        return new ScoutGroupDto(
                scoutGroup.getName(),
                scoutGroup.getProvinceId(),
                scoutGroup.getGroupNumber()
        );
    }
}
