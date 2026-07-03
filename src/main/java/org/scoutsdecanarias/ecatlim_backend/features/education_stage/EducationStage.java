package org.scoutsdecanarias.ecatlim_backend.features.education_stage;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.scoutsdecanarias.ecatlim_backend.features.module.Module;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class EducationStage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String code;

    @Column(length = 1000, nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer onlineHours;

    @Column(nullable = false)
    private Integer contactHours;

    @Column(nullable = false)
    private Integer practicalHours;

    @OneToMany(mappedBy = "educationStage", cascade = CascadeType.ALL)
    private List<Module> modules = new ArrayList<>();

    private boolean previousStageRequired;

    @ManyToOne
    private EducationStage previousStage;
}
