package org.scoutsdecanarias.ecatlim_backend.features.activity.repository;

import org.scoutsdecanarias.ecatlim_backend.features.activity.entity.FileSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileSubmissionRepository extends JpaRepository<FileSubmission,Integer> {
}
