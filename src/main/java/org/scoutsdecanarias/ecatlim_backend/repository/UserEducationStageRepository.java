package org.scoutsdecanarias.ecatlim_backend.repository;

import org.scoutsdecanarias.ecatlim_backend.entity.User;
import org.scoutsdecanarias.ecatlim_backend.entity.UserEducationStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserEducationStageRepository extends JpaRepository<UserEducationStage, Integer> {
    Optional<UserEducationStage> getUserEducationStagesCompletedByUser(User user);
}
