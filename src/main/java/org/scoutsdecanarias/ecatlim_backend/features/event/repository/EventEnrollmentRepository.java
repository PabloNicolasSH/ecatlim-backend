package org.scoutsdecanarias.ecatlim_backend.features.event.repository;

import org.scoutsdecanarias.ecatlim_backend.features.event.entity.EventEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventEnrollmentRepository extends JpaRepository<EventEnrollment, Integer> {
    boolean existsByUserIdAndEventIdAndLessonBlockId(Integer userId, Integer eventId, Integer lessonBlockId);
    void deleteByUserIdAndEventIdAndLessonBlockId(Integer userId, Integer eventId, Integer lessonBlockId);

    @Query("SELECT ee FROM EventEnrollment ee " +
            "JOIN FETCH ee.event e " +
            "JOIN FETCH ee.lessonBlock lb " +
            "JOIN FETCH lb.module m " +
            "WHERE ee.user.email = :email " +
            "AND ee.hasAttended = true " +
            "AND m.educationStage.id = :stageId " +
            "ORDER BY e.startDate DESC")
    List<EventEnrollment> findAttendedByUserEmailAndStageId(
            @Param("email") String email,
            @Param("stageId") Integer stageId
    );

    @Query("SELECT DISTINCT ee.user.id FROM EventEnrollment ee " +
            "WHERE ee.event.id = :eventId " +
            "AND ee.lessonBlock.id = :lessonBlockId ")
    List<Integer> findStudentIdsByEventAndLessonBlock(
            @Param("eventId") Integer eventId,
            @Param("lessonBlockId") Integer lessonBlockId
    );
}
