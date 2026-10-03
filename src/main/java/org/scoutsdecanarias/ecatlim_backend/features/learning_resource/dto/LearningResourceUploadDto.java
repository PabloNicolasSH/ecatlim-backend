package org.scoutsdecanarias.ecatlim_backend.features.learning_resource.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.ResourceType;

import java.util.List;

public record LearningResourceUploadDto(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String name,

        @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
        String description,

        @Size(max = 255, message = "El enlace no puede superar los 255 caracteres")
        String blobPath,

        @NotNull(message = "El tipo de recurso es obligatorio")
        ResourceType type,

        @Size(max = 20, message = "Un recurso no puede tener más de 20 etiquetas")
        List<@NotBlank @Size(max = 255, message = "Una etiqueta no puede superar los 255 caracteres") String> tagNames
) {
    @AssertTrue(message = "Para un enlace o vídeo debes indicar una URL que empiece por http:// o https://")
    public boolean isLinkValid() {
        boolean isLink = type == ResourceType.LINK || type == ResourceType.VIDEO_LINK;
        return !isLink || (blobPath != null && blobPath.matches("^https?://\\S+$"));
    }
}
