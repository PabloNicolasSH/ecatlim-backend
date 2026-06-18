package org.scoutsdecanarias.ecatlim_backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;
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
