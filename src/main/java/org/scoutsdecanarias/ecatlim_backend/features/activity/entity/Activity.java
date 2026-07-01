package org.scoutsdecanarias.ecatlim_backend.features.activity.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.scoutsdecanarias.ecatlim_backend.features.activity.enums.ActivityType;
import org.scoutsdecanarias.ecatlim_backend.features.activity.enums.EvaluationMethod;
import org.scoutsdecanarias.ecatlim_backend.features.event.entity.Event;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.LessonBlock;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private ActivityType activityType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EvaluationMethod evaluationMethod = EvaluationMethod.AUTOMATIC;

    private Boolean isOptional = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime availableAt;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_block_id", nullable = false)
    private LessonBlock lessonBlock;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "activity_correctors",
            joinColumns = @JoinColumn(name = "activity_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> correctors = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;
}
