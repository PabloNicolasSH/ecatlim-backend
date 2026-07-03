package org.scoutsdecanarias.ecatlim_backend.features.user.repository;

import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.LessonBlock;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.UserLessonBlock;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserLessonBlockRepository extends JpaRepository<UserLessonBlock, Integer> {
    Optional<UserLessonBlock> findByUserIdAndLessonBlockId(Integer id, Integer lbId);

    boolean existsByUserAndLessonBlockAndCompletedTrue(User user, LessonBlock lessonBlock);

    @Query("""
        SELECT COUNT(ulb) FROM UserLessonBlock ulb
            WHERE ulb.user.id = :userId
            AND ulb.lessonBlock.module.educationStage.id = :stageId
            AND ulb.completed = true
    """)
    int countCompletedByUserIdAndStageId(@Param("userId") Integer userId, @Param("stageId") Integer stageId);

    @Query("""
        SELECT u FROM User u
        WHERE u.enabled = true
        AND EXISTS (
            SELECT 1 FROM UserLessonBlock ulb
            WHERE ulb.user = u
            AND ulb.lessonBlock IN :eventLessonBlocks
            AND ulb.completed = false
        )
    """)
    List<User> findActiveAndEligibleUsers(Set<LessonBlock> eventLessonBlocks);
}
