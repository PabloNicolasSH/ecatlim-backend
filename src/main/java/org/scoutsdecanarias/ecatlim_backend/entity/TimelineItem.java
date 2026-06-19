package org.scoutsdecanarias.ecatlim_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.scoutsdecanarias.ecatlim_backend.features.event.entity.Event;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class TimelineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType itemType;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "education_session_id", referencedColumnName = "id")
    private EducationSession educationSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    public enum ItemType{FORMATIVE, LOGISTIC}

    public boolean isFormative() {
        return ItemType.FORMATIVE.equals(this.itemType);
    }
}