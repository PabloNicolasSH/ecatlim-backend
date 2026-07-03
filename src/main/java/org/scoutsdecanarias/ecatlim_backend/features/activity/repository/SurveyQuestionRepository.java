package org.scoutsdecanarias.ecatlim_backend.features.activity.repository;

import org.scoutsdecanarias.ecatlim_backend.features.activity.entity.SurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Integer> {
}
