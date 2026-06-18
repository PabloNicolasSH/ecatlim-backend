package org.scoutsdecanarias.ecatlim_backend.features.user.repository;

import org.scoutsdecanarias.ecatlim_backend.features.user.entity.UserEducationStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserEducationStageRepository extends JpaRepository<UserEducationStage, Integer> {

    List<UserEducationStage> findByUser_Email(String userEmail);
    List<UserEducationStage> findByUserId(Integer id);
    boolean existsByUserIdAndEducationStageId(Integer userId, Integer stageId);
}
