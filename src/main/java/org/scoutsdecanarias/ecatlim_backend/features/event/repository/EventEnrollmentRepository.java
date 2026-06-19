package org.scoutsdecanarias.ecatlim_backend.features.event.repository;

import org.scoutsdecanarias.ecatlim_backend.features.event.entity.EventEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventEnrollmentRepository extends JpaRepository<EventEnrollment, Integer> {
    boolean existsByUserIdAndEventIdAndLessonBlockId(Integer userId, Integer eventId, Integer lessonBlockId);
    void deleteByUserIdAndEventIdAndLessonBlockId(Integer userId, Integer eventId, Integer lessonBlockId);
}
