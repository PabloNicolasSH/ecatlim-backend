package org.scoutsdecanarias.ecatlim_backend.repository;

import org.scoutsdecanarias.ecatlim_backend.entity.UserLessonBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserLessonBlockRepository extends JpaRepository<UserLessonBlock, Integer> {
    Optional<UserLessonBlock> findByUserIdAndLessonBlockId(Integer id, Integer lbId);
}
