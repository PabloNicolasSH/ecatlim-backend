package org.scoutsdecanarias.ecatlim_backend.features.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.scoutsdecanarias.ecatlim_backend.features.education_stage.UserEducationStage;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.UserLessonBlock;
import org.scoutsdecanarias.ecatlim_backend.features.user.enums.Role;

import java.util.List;

@Getter
@Setter
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private boolean enabled = true;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserProfile profile;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserLessonBlock> lessonBlocks;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserEducationStage> educationStages;
}
