package org.scoutsdecanarias.ecatlim_backend.features.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import org.scoutsdecanarias.ecatlim_backend.entity.EducationStage;

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