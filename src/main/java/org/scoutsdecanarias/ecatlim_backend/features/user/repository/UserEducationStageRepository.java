package org.scoutsdecanarias.ecatlim_backend.features.user.repository;

import org.scoutsdecanarias.ecatlim_backend.features.education_stage.UserEducationStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserEducationStageRepository extends JpaRepository<UserEducationStage, Integer> {

    List<UserEducationStage> findByUser_Email(String userEmail);
    List<UserEducationStage> findByUserId(Integer id);
    boolean existsByUserIdAndEducationStageId(Integer userId, Integer stageId);
    Optional<UserEducationStage> findByPersonalPlanId(Integer fileId);
    Optional<UserEducationStage> findByEntityApprovalId(Integer fileId);
}
