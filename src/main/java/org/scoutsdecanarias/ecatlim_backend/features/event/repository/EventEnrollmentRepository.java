package org.scoutsdecanarias.ecatlim_backend.features.event.repository;

import org.scoutsdecanarias.ecatlim_backend.features.event.entity.EventEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventEnrollmentRepository extends JpaRepository<EventEnrollment, Integer> {
    boolean existsByUserId(Integer userId);

    @Query("SELECT ee FROM EventEnrollment ee WHERE ee.user.id = :userId AND ee.attendance IS NULL AND ee.event.endDate > :now")
List<EventEnrollment> findUpcomingByUserId(@Param("userId") Integer userId, @Param("now") java.time.LocalDateTime now);

@Query("SELECT ee FROM EventEnrollment ee WHERE ee.user.id = :userId AND ee.lessonBlock.id = :lessonBlockId AND ee.attendance IS NULL AND ee.event.endDate > :now")
List<EventEnrollment> findUpcomingByUserIdAndLessonBlockId(@Param("userId") Integer userId, @Param("lessonBlockId") Integer lessonBlockId, @Param("now") java.time.LocalDateTime now);
    boolean existsByUserIdAndEventIdAndLessonBlockId(Integer userId, Integer eventId, Integer lessonBlockId);
    Optional<EventEnrollment> findByUserIdAndEventIdAndLessonBlockId(Integer userId, Integer eventId, Integer lessonBlockId);
    void deleteByUserIdAndEventIdAndLessonBlockId(Integer userId, Integer eventId, Integer lessonBlockId);

    @Query("SELECT ee FROM EventEnrollment ee " +
            "JOIN FETCH ee.event e " +
            "JOIN FETCH ee.lessonBlock lb " +
            "JOIN FETCH lb.module m " +
            "WHERE ee.user.email = :email " +
            "AND ee.attendance IS NOT NULL " +
            "AND m.educationStage.id = :stageId " +
            "ORDER BY e.startDate DESC")
    List<EventEnrollment> findAttendedByUserEmailAndStageId(
            @Param("email") String email,
            @Param("stageId") Integer stageId
    );

    @Query("SELECT ee FROM EventEnrollment ee " +
            "JOIN FETCH ee.event e " +
            "JOIN FETCH ee.lessonBlock lb " +
            "JOIN FETCH lb.module m " +
            "JOIN FETCH m.educationStage " +
            "WHERE ee.user.id = :userId " +
            "AND ee.attendance IS NOT NULL " +
            "ORDER BY e.startDate ASC")
    List<EventEnrollment> findAttendedByUserId(@Param("userId") Integer userId);

    @Query("SELECT DISTINCT ee.user.id FROM EventEnrollment ee " +
            "WHERE ee.event.id = :eventId " +
            "AND ee.lessonBlock.id = :lessonBlockId ")
    List<Integer> findStudentIdsByEventAndLessonBlock(
            @Param("eventId") Integer eventId,
            @Param("lessonBlockId") Integer lessonBlockId
    );

    @Query("SELECT ee FROM EventEnrollment ee " +
            "JOIN FETCH ee.event e " +
            "JOIN FETCH ee.lessonBlock lb " +
            "JOIN FETCH ee.user u " +
            "JOIN FETCH u.profile p " +
            "WHERE p.scoutGroup.id = :scoutGroupId " +
            "AND u.enabled = true " +
            "ORDER BY e.startDate DESC")
    List<EventEnrollment> findAllByUserScoutGroup(@Param("scoutGroupId") Integer scoutGroupId);
}
