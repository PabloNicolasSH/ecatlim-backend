package org.scoutsdecanarias.ecatlim_backend.features.news.dto;

import org.scoutsdecanarias.ecatlim_backend.features.news.entity.NewsAttachment;

public record NewsAttachmentDto(
        Integer id,
        String name,
        String mimeType,
        long sizeBytes
) {
    public static NewsAttachmentDto fromEntity(NewsAttachment attachment) {
        return new NewsAttachmentDto(attachment.getId(), attachment.getName(), attachment.getMimeType(), attachment.getSizeBytes());
    }
}
