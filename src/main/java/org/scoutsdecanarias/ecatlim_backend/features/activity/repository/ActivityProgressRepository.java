package org.scoutsdecanarias.ecatlim_backend.features.activity.repository;

import org.scoutsdecanarias.ecatlim_backend.features.activity.dto.PendingActivityRow;
import org.scoutsdecanarias.ecatlim_backend.features.activity.entity.ActivityProgress;
import org.scoutsdecanarias.ecatlim_backend.features.activity.enums.ProgressStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityProgressRepository extends JpaRepository<ActivityProgress,Integer> {

    Optional<ActivityProgress> findByActivityIdAndStudentId(Integer activityId, Integer studentId);

    @Query("""
            select new org.scoutsdecanarias.ecatlim_backend.features.activity.dto.PendingActivityRow(
                p.studentId, a.title, a.dueDate, e.title)
            from ActivityProgress p
            join p.activity a
            left join a.event e
            where p.status = :status and a.availableAt <= :now
            order by a.dueDate
            """)
    List<PendingActivityRow> findAvailableByStatus(@Param("status") ProgressStatus status, @Param("now") LocalDateTime now);
}
