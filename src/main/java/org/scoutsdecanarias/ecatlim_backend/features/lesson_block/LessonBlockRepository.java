package org.scoutsdecanarias.ecatlim_backend.features.lesson_block;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonBlockRepository extends JpaRepository<LessonBlock, Integer> {
    @Query("SELECT COUNT(lb) FROM LessonBlock lb " +
            "WHERE lb.module.educationStage.id = :stageId")
    int countByStageId(@Param("stageId") Integer stageId);
}