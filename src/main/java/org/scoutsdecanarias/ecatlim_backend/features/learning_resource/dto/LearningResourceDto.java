package org.scoutsdecanarias.ecatlim_backend.features.learning_resource.dto;

import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.entity.LearningResource;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public record LearningResourceDto(
    Integer id,
    String name,
    String description,
    String type,
    String blobPath,
    String mimeType,
    List<TagDto> tags,
    LocalDateTime createdAt,
    String uploadedBy,
    int downloadCount,
    boolean canManage
) {

    public static LearningResourceDto fromEntity(LearningResource entity, boolean canManage) {
        return new LearningResourceDto(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getResourceType().toString(),
            entity.getBlobPath(),
            entity.getMimeType(),
            TagDto.fromCollection(entity.getTags()).stream()
                    .sorted(Comparator.comparing(TagDto::name, String.CASE_INSENSITIVE_ORDER))
                    .toList(),
            entity.getCreatedAt(),
            uploaderName(entity.getUser()),
            entity.getDownloadCount(),
            canManage
        );
    }

    public static List<LearningResourceDto> fromCollection(List<LearningResource> all, Predicate<LearningResource> canManage) {
        return all.stream().map(resource -> fromEntity(resource, canManage.test(resource))).toList();
    }

    private static String uploaderName(User user) {
        if (user == null || user.getProfile() == null) {
            return null;
        }
        return (user.getProfile().getName() + " " + user.getProfile().getSurname()).trim();
    }
}
