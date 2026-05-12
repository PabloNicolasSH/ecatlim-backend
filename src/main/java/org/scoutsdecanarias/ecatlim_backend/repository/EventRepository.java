package org.scoutsdecanarias.ecatlim_backend.repository;

import org.scoutsdecanarias.ecatlim_backend.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.timelineItems WHERE e.id = :id")
    Optional<Event> findByIdWithTimeline(Integer id);
}
