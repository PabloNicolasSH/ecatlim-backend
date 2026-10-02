package org.scoutsdecanarias.ecatlim_backend.features.education_stage;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user_file.UserFile;

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

    @ManyToOne
    @JoinColumn(name = "tutor_id")
    private User tutor;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "personal_plan_id", referencedColumnName = "id")
    private UserFile personalPlan;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "entity_approval_id", referencedColumnName = "id")
    private UserFile entityApproval;

    public enum StageStatus {
        ENROLLED, IN_PROGRESS, COMPLETED, DROPPED;
    }
}