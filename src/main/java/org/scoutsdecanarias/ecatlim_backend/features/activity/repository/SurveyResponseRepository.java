package org.scoutsdecanarias.ecatlim_backend.features.activity.repository;

import org.scoutsdecanarias.ecatlim_backend.features.activity.entity.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyResponseRepository extends JpaRepository<SurveyResponse,Integer> {
    @Query("SELECT COALESCE(MAX(sr.attemptNumber), 0) FROM SurveyResponse sr WHERE sr.student.id = :studentId AND sr.question.id = :questionId")
    Integer findMaxAttemptByStudentAndQuestion(
            @Param("studentId") Integer studentId,
            @Param("questionId") Integer questionId
    );
}
