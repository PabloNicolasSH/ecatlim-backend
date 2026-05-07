package org.scoutsdecanarias.ecatlim_backend.repository;

import org.scoutsdecanarias.ecatlim_backend.entity.UserLessonBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserLessonBlockRepository extends JpaRepository<UserLessonBlock, Integer> {
    Optional<UserLessonBlock> findByUserIdAndLessonBlockId(Integer id, Integer lbId);

    @Query("SELECT COUNT(ulb) FROM UserLessonBlock ulb " +
            "WHERE ulb.user.id = :userId " +
            "AND ulb.lessonBlock.module.educationStage.id = :stageId " +
            "AND ulb.completed = true")
    int countCompletedByUserIdAndStageId(@Param("userId") Integer userId, @Param("stageId") Integer stageId);
}
