package org.scoutsdecanarias.ecatlim_backend.features.learning_resource.dto;

import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.ResourceType;

import java.util.List;

public record LearningResourceUploadDto(
        String name,
        String description,
        String blobPath,
        ResourceType type,
        List<String> tagNames
) {
}
