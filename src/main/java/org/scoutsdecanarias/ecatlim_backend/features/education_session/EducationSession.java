package org.scoutsdecanarias.ecatlim_backend.features.education_session;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.scoutsdecanarias.ecatlim_backend.features.timeline.TimelineItem;
import org.scoutsdecanarias.ecatlim_backend.features.activity.entity.Activity;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.LessonBlock;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;

import java.util.List;

@Getter
@Setter
@Entity
public class EducationSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToMany
    @JoinTable(
            name = "education_session_facilitators",
            joinColumns = @JoinColumn(name = "education_session_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> facilitators;

    @ManyToOne
    @JoinColumn(name = "lesson_block_id")
    private LessonBlock lessonBlock;

    @OneToMany
    private List<Activity> activities;

    @OneToOne(mappedBy = "educationSession")
    private TimelineItem timelineItem;
}
