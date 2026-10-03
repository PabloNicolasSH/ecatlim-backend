package org.scoutsdecanarias.ecatlim_backend.features.learning_resource.repository;

import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.entity.LearningResource;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningResourceRepository extends JpaRepository<LearningResource, Integer> {

    @EntityGraph(attributePaths = {"tags", "user", "user.profile"})
    List<LearningResource> findAllByOrderByIdDesc();

    @Modifying
    @Query("UPDATE LearningResource r SET r.downloadCount = r.downloadCount + 1 WHERE r.id = :id")
    void incrementDownloadCount(@Param("id") Integer id);
}
