package org.scoutsdecanarias.ecatlim_backend.features.education_stage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EducationStageRepository extends JpaRepository<EducationStage, Integer> {
    EducationStage findEducationStageById(Integer id);
}
