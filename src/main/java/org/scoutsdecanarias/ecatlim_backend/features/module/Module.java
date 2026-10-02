package org.scoutsdecanarias.ecatlim_backend.features.module;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.scoutsdecanarias.ecatlim_backend.features.education_stage.EducationStage;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.LessonBlock;

import java.util.ArrayList;
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

    @Column(nullable = false)
    private Integer moduleId;

    @Column(length = 1000, nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModuleType type;

    @Column(nullable = false)
    private Integer onlineHours;

    @Column(nullable = false)
    private Integer contactHours;

    @ManyToOne
    @JoinColumn(name = "education_stage_id")
    private EducationStage educationStage;

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL)
    private List<LessonBlock> lessonBlocks = new ArrayList<>();

    public String getCode() {
        StringBuilder code = new StringBuilder("M");

        if (type.equals(ModuleType.THEORETICAL)) {
            code.append("F");
        } else {
            code.append("P");
        }

        if (educationStage != null) {
            code.append(educationStage.getCode());
        }

        code.append("-");
        code.append(moduleId);

        return code.toString();
    }
}
