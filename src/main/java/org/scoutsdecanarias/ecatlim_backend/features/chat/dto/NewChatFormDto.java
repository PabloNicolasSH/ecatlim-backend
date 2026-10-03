package org.scoutsdecanarias.ecatlim_backend.features.chat.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record NewChatFormDto(
        @Size(max = 255, message = "El nombre del grupo no puede superar los 255 caracteres")
        String name,

        @Size(max = 255, message = "La descripción del grupo no puede superar los 255 caracteres")
        String description,

        @NotEmpty(message = "Debes seleccionar al menos un participante")
        @Size(max = 200, message = "Un chat no puede tener más de 200 participantes")
        List<@NotNull Integer> chatMembers
) {
}
