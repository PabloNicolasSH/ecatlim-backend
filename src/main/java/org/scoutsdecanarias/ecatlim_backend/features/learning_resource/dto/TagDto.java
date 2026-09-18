package org.scoutsdecanarias.ecatlim_backend.features.learning_resource.dto;

import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.entity.Tag;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record TagDto(
        Integer id,
        String name
) {
    public static TagDto fromEntity(Tag entity) {
        return new TagDto(entity.getId(), entity.getName());
    }

    public static List<TagDto> fromCollection(Set<Tag> entities) {
        return entities.stream().map(TagDto::fromEntity).collect(Collectors.toList());
    }

    public static List<TagDto> fromCollection(List<Tag> entities) {
        return entities.stream().map(TagDto::fromEntity).collect(Collectors.toList());
    }
}
