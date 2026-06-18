package org.scoutsdecanarias.ecatlim_backend.features.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import org.scoutsdecanarias.ecatlim_backend.entity.LessonBlock;

import java.util.Date;

@Getter
@Setter
@Entity
public class UserLessonBlock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private LessonBlock lessonBlock;

    @ManyToOne
    private User user;

    private boolean completed = false;

    private Date completionDate;

    private Date enrollmentDate;
}