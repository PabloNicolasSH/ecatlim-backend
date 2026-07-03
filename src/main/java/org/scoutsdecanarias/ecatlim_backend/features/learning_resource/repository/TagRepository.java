package org.scoutsdecanarias.ecatlim_backend.features.learning_resource.repository;

import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag,Integer> {
    List<Tag> findByNameIn(List<String> names);

    boolean existsByNameIgnoreCase(String sanitizedName);
}
