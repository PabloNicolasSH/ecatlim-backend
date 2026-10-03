package org.scoutsdecanarias.ecatlim_backend.features.activity.dto;

import org.scoutsdecanarias.ecatlim_backend.features.activity.entity.ForumPublication;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.SimpleUserDto;

import java.time.LocalDateTime;
import java.util.List;

public record ForumPublicationDto(
        Integer id,
        String title,
        String body,
        LocalDateTime publishedAt,
        SimpleUserDto author
) {
    public static ForumPublicationDto fromEntity(ForumPublication publication) {
        return new ForumPublicationDto(
                publication.getId(),
                publication.getTitle(),
                publication.getBody(),
                publication.getPublishedAt(),
                publication.getAuthor() != null ? SimpleUserDto.fromEntity(publication.getAuthor()) : null
        );
    }

    public static List<ForumPublicationDto> fromCollection(List<ForumPublication> publications) {
        return publications.stream().map(ForumPublicationDto::fromEntity).toList();
    }
}
