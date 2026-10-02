package org.scoutsdecanarias.ecatlim_backend.features.education_stage;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;

import java.util.Date;

@Getter
@Setter
@Entity
public class UserEducationStage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private User user;

    @ManyToOne
    private EducationStage educationStage;

    private Date enrollmentDate;

    private boolean completed = false;

    private Date completionDate;

    @Enumerated(EnumType.STRING)
    private StageStatus status;

    public enum StageStatus {
        ENROLLED, IN_PROGRESS, COMPLETED, DROPPED;
    }
}