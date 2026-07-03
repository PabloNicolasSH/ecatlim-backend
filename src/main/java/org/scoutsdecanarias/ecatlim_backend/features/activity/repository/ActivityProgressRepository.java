package org.scoutsdecanarias.ecatlim_backend.features.activity.repository;

import org.scoutsdecanarias.ecatlim_backend.features.activity.entity.ActivityProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActivityProgressRepository extends JpaRepository<ActivityProgress,Integer> {

    Optional<ActivityProgress> findByActivityIdAndStudentId(Integer activityId, Integer studentId);
}
