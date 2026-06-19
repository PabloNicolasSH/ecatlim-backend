package org.scoutsdecanarias.ecatlim_backend.features.activity.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.scoutsdecanarias.ecatlim_backend.features.activity.enums.ActivityType;
import org.scoutsdecanarias.ecatlim_backend.features.activity.enums.EvaluationMethod;
import org.scoutsdecanarias.ecatlim_backend.features.event.entity.Event;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType activityType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EvaluationMethod evaluationMethod = EvaluationMethod.AUTOMATIC;

    private Boolean isOptional = false;

    private Boolean isGradable = false;

    private Integer maxAttempts;

    private Double passingScore;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime availableAt;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL)
    private List<ForumPublication> publications;

    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL)
    private List<SurveyQuestion> surveyQuestions;

    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL)
    private List<FileSubmission> fileSubmissions;
}
