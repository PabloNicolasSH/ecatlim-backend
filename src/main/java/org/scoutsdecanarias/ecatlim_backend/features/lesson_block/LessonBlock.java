package org.scoutsdecanarias.ecatlim_backend.features.lesson_block;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.scoutsdecanarias.ecatlim_backend.entity.Module;
import org.scoutsdecanarias.ecatlim_backend.enums.ModuleType;

import java.util.List;

@Getter
@Setter
@Entity
public class LessonBlock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer lessonBlockId;

    @Column(length = 1000, nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer onlineHours;

    @Column(nullable = false)
    private Integer contactHours;

    private boolean recognizable;

    @ManyToOne
    @JoinColumn(name = "module_id")
    private Module module;

    @OneToMany(mappedBy = "lessonBlock", cascade = CascadeType.ALL)
    private List<UserLessonBlock> userLessonBlocks;

    public String getCode() {
        StringBuilder code = new StringBuilder("B");

        if (this.module != null && this.module.getType() != null) {
            if (this.module.getType().equals(ModuleType.THEORETICAL)) {
                code.append("F");
            } else {
                code.append("P");
            }
        }

        if (this.module != null && this.module.getEducationStage() != null) {
            code.append(this.module.getEducationStage().getCode());
        }

        code.append("-");
        code.append(this.lessonBlockId);

        return code.toString();
    }

    public Integer getEducationStageId() {
        if (this.module != null && this.module.getEducationStage() != null) {
            return this.module.getEducationStage().getId();
        }
        return null;
    }
}
