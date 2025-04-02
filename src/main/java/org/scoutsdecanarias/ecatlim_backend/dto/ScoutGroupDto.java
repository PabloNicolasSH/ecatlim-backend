package org.scoutsdecanarias.ecatlim_backend.dto;

import org.scoutsdecanarias.ecatlim_backend.entity.ScoutGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record ScoutGroupDto(Integer id, String name, int provinceId, int groupNumber) {

    public static ScoutGroupDto fromEntity(ScoutGroup scoutGroup) {
        if (scoutGroup == null) return null;
        return new ScoutGroupDto(
                scoutGroup.getId(),
                scoutGroup.getName(),
                scoutGroup.getProvinceId(),
                scoutGroup.getGroupNumber()
        );
    }

    public static List<ScoutGroupDto> fromCollection(List<ScoutGroup> groups) {
        return groups.stream().map(ScoutGroupDto::fromEntity).collect(Collectors.toList());
    }
}
