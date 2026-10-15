package org.scoutsdecanarias.ecatlim_backend.features.news.dto;

import org.scoutsdecanarias.ecatlim_backend.features.news.entity.News;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public record NewsDto(
        Integer id,
        String title,
        String content,
        boolean hasImage,
        String author,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<NewsAttachmentDto> attachments
) {
    public static NewsDto fromEntity(News news) {
        return new NewsDto(
                news.getId(),
                news.getTitle(),
                news.getContent(),
                news.hasImage(),
                authorName(news.getAuthor()),
                news.getCreatedAt(),
                news.getUpdatedAt(),
                news.getAttachments().stream().map(NewsAttachmentDto::fromEntity).toList()
        );
    }

    public static List<NewsDto> fromCollection(List<News> news) {
        return news.stream().map(NewsDto::fromEntity).toList();
    }

    private static String authorName(User author) {
        if (author == null || author.getProfile() == null) {
            return null;
        }
        return (author.getProfile().getName() + " " + author.getProfile().getSurname()).trim();
    }
}
