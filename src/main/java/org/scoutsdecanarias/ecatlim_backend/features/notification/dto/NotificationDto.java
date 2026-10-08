package org.scoutsdecanarias.ecatlim_backend.features.notification.dto;

import org.scoutsdecanarias.ecatlim_backend.features.notification.entity.Notification;
import org.scoutsdecanarias.ecatlim_backend.features.notification.enums.NotificationType;

import java.time.LocalDateTime;

public record NotificationDto(
        Integer id,
        NotificationType type,
        String title,
        String description,
        String link,
        boolean requiresAction,
        boolean read,
        boolean pending,
        LocalDateTime createdAt
) {
    public static NotificationDto from(Notification n) {
        return new NotificationDto(n.getId(), n.getType(), n.getTitle(), n.getDescription(), n.getLink(),
                n.isRequiresAction(), n.getReadAt() != null, n.isPending(), n.getCreatedAt());
    }
}
