package org.scoutsdecanarias.ecatlim_backend.features.lesson_block;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;

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