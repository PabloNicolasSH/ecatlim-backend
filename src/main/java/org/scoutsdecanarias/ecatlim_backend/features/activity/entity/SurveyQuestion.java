package org.scoutsdecanarias.ecatlim_backend.features.activity.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.scoutsdecanarias.ecatlim_backend.features.activity.enums.SurveyResponseType;

import java.util.List;

@Getter
@Setter
@Entity
public class SurveyQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @Column(nullable = false)
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SurveyResponseType responseType;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    private List<SurveyOption> options;
}
