package org.scoutsdecanarias.ecatlim_backend.repository;

import org.scoutsdecanarias.ecatlim_backend.entity.User;
import org.scoutsdecanarias.ecatlim_backend.entity.UserEducationStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserEducationStageRepository extends JpaRepository<UserEducationStage, Integer> {

    List<UserEducationStage> findByUser_Email(String userEmail);
    List<UserEducationStage> findByUserId(Integer id);
    boolean existsByUserIdAndEducationStageId(Integer userId, Integer stageId);
}
