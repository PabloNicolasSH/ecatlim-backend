package org.scoutsdecanarias.ecatlim_backend.features.news.repository;

import org.scoutsdecanarias.ecatlim_backend.features.news.entity.News;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Integer> {
    @EntityGraph(attributePaths = {"author", "author.profile", "attachments"})
    List<News> findAllByOrderByCreatedAtDescIdDesc(Pageable pageable);
}
