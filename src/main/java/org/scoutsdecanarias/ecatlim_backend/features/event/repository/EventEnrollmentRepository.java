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

    @Query("SELECT DISTINCT ee.user.id FROM EventEnrollment ee " +
            "WHERE ee.event.id = :eventId " +
            "AND ee.lessonBlock.id = :lessonBlockId ")
    List<Integer> findStudentIdsByEventAndLessonBlock(
            @Param("eventId") Integer eventId,
            @Param("lessonBlockId") Integer lessonBlockId
    );
}
