package org.scoutsdecanarias.ecatlim_backend.features.event.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.scoutsdecanarias.ecatlim_backend.features.event.enums.EventContactType;
import org.scoutsdecanarias.ecatlim_backend.features.scout_group.ScoutGroup;

@Getter
@Setter
@Entity
public class EventContact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventContactType type;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_entity_id")
    private ScoutGroup originEntity;

    private String specialty;
}
