package org.scoutsdecanarias.ecatlim_backend.features.onboarding;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "onboarding_progress")
public class OnboardingProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "progress_key", nullable = false, length = 80)
    private String progressKey;

    @Column(nullable = false)
    private LocalDateTime completedAt;
}
