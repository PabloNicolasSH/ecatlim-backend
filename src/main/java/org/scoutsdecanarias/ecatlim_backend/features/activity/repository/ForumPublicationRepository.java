package org.scoutsdecanarias.ecatlim_backend.features.activity.repository;

import org.scoutsdecanarias.ecatlim_backend.features.activity.entity.ForumPublication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumPublicationRepository extends JpaRepository<ForumPublication,Integer> {
    List<ForumPublication> findByActivityIdOrderByTitleAsc(Integer activityId);
    List<ForumPublication> findByActivityIdOrderByPublishedAtDesc(Integer activityId);
}
