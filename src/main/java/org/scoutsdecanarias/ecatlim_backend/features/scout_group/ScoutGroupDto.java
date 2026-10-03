package org.scoutsdecanarias.ecatlim_backend.features.scout_group;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.stream.Collectors;

public record ScoutGroupDto(
        Integer id,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 255, message = "El nombre debe tener entre 3 y 255 caracteres")
        String name,

        int provinceId,

        @Min(value = 0, message = "El número de grupo no puede ser negativo")
        @Max(value = 9999, message = "El número de grupo no puede superar 9999")
        int groupNumber,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        @Size(max = 255, message = "El email no puede superar los 255 caracteres")
        String email
) {

    @JsonIgnore
    @AssertTrue(message = "La provincia debe ser Las Palmas o Santa Cruz de Tenerife")
    public boolean isProvinceValid() {
        return provinceId == 35 || provinceId == 38;
    }

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
