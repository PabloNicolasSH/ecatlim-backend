package org.scoutsdecanarias.ecatlim_backend.features.event.repository;

import org.scoutsdecanarias.ecatlim_backend.features.event.entity.Event;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.event.enums.EventStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
    List<Event> findAllByStatus(EventStatus eventStatus);

    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.timelineItems WHERE e.id = :id")
    Optional<Event> findByIdWithTimeline(Integer id);

    @Query("SELECT e FROM Event e WHERE e.startDate > :now ORDER BY e.startDate ASC")
    List<Event> findUpcomingEvents(LocalDateTime now, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.startDate > :now AND :user MEMBER OF e.attendees")
    List<Event> findUpcomingEventsByUser(LocalDateTime now, User user, Pageable pageable);
}
