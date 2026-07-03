package org.scoutsdecanarias.ecatlim_backend.features.learning_resource.dto;

import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.entity.LearningResource;

import java.util.List;
import java.util.stream.Collectors;

public record LearningResourceDto(
        Integer id,
        String name,
        String description,
        String type,
        String blobPath,
        List<TagDto> tags
) {

    public static LearningResourceDto fromEntity(LearningResource entity) {
        return new LearningResourceDto(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getResourceType().toString(),
                entity.getBlobPath(),
                TagDto.fromCollection(entity.getTags())
        );
    }

    public static List<LearningResourceDto> fromCollection(List<LearningResource> all) {
        return all.stream().map(LearningResourceDto::fromEntity).collect(Collectors.toList());
    }
}
