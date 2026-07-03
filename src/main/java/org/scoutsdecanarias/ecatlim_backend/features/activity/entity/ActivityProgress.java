package org.scoutsdecanarias.ecatlim_backend.features.activity.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.scoutsdecanarias.ecatlim_backend.features.activity.enums.ProgressStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class ActivityProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id")
    private Activity activity;

    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    @Enumerated(EnumType.STRING)
    private ProgressStatus status = ProgressStatus.PENDING;

    private LocalDateTime updatedAt;

    private Double score;
}
