package org.scoutsdecanarias.ecatlim_backend.features.learning_resource.repository;

import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.entity.LearningResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LearningResourceRepository extends JpaRepository<LearningResource, Integer> {
}
