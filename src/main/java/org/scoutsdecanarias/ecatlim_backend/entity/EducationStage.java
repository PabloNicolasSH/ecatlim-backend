package org.scoutsdecanarias.ecatlim_backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

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
