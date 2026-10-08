package org.scoutsdecanarias.ecatlim_backend.features.notification.entity;

import org.scoutsdecanarias.ecatlim_backend.features.notification.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(length = 500)
    private String description;

    private String link;

    /**
     * Informative notifications are pending until read; action notifications stay pending
     * until the associated action is fulfilled, regardless of being read.
     */
    @Column(nullable = false)
    private boolean requiresAction;

    private Integer referenceId;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime readAt;

    private LocalDateTime resolvedAt;

    public boolean isPending() {
        return requiresAction ? resolvedAt == null : readAt == null;
    }
}
