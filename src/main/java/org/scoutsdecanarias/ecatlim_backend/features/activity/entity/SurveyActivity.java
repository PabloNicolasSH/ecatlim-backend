package org.scoutsdecanarias.ecatlim_backend.features.activity.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@PrimaryKeyJoinColumn(name = "activity_id")
public class SurveyActivity extends Activity {

    private Boolean isGradable = false;

    private Integer maxAttempts;

    private Double passingScore;

    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL)
    private List<SurveyQuestion> surveyQuestions;
}
