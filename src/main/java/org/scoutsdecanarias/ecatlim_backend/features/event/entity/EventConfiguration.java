package org.scoutsdecanarias.ecatlim_backend.features.event.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.scoutsdecanarias.ecatlim_backend.features.event.enums.NotificationTarget;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
public class EventConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer minParticipants;

    @Column(nullable = false)
    private LocalDateTime dateOpenInscription;

    @Column(nullable = false)
    private LocalDateTime dateCloseInscription;

    @Column(nullable = false)
    private Integer cost;

    @Column(nullable = false)
    private String transferBankNumber;

    @Column(nullable = false)
    private String transferCode;

    @ElementCollection(targetClass = NotificationTarget.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "event_configuration_notifications",
            joinColumns = @JoinColumn(name = "event_configuration_id")
    )
    @Column(name = "notification_target", nullable = false)
    private Set<NotificationTarget> notificationTarget = new HashSet<>();
}
