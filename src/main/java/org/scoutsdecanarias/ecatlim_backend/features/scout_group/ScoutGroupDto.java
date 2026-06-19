package org.scoutsdecanarias.ecatlim_backend.features.scout_group;

import java.util.List;
import java.util.stream.Collectors;

public record ScoutGroupDto(Integer id, String name, int provinceId, int groupNumber, String email) {

    public static ScoutGroupDto fromEntity(ScoutGroup scoutGroup) {
        if (scoutGroup == null) return null;
        return new ScoutGroupDto(
                scoutGroup.getId(),
                scoutGroup.getName(),
                scoutGroup.getProvinceId(),
                scoutGroup.getGroupNumber(),
                scoutGroup.getEmail()
        );
    }

    public static List<ScoutGroupDto> fromCollection(List<ScoutGroup> groups) {
        return groups.stream().map(ScoutGroupDto::fromEntity).collect(Collectors.toList());
    }
}