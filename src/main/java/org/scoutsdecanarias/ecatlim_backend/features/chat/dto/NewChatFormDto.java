package org.scoutsdecanarias.ecatlim_backend.features.chat.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record NewChatFormDto(
        String name,
        String description,
        @NotEmpty List<Integer> chatMembers
) {
}
