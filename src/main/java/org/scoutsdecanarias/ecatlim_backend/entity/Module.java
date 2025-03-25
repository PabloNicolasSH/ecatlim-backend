package org.scoutsdecanarias.ecatlim_backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;
import org.scoutsdecanarias.ecatlim_backend.enums.ModuleType;

import java.util.List;

@Getter
@Setter
@Entity
public class Module {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000, nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModuleType type;

    private Integer learningHours;

    @ManyToOne
    @JoinColumn(name = "education_stage_id")
    private EducationStage educationStage;

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL)
    private List<LessonBlock> lessonBlocks;
}
