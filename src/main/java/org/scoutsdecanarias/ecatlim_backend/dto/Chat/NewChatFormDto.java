package org.scoutsdecanarias.ecatlim_backend.dto.Chat;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record NewChatFormDto(
        String name,
        String description,
        @NotEmpty List<Integer> chatMembers
) {
}
