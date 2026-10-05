package org.scoutsdecanarias.ecatlim_backend.features.recognition.repository;

import org.scoutsdecanarias.ecatlim_backend.features.recognition.entity.RecognitionRequest;
import org.scoutsdecanarias.ecatlim_backend.features.recognition.enums.RecognitionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecognitionRequestRepository extends JpaRepository<RecognitionRequest, Integer> {
    List<RecognitionRequest> findByUserIdOrderByCreatedAtDesc(Integer userId);

    List<RecognitionRequest> findByStatusInOrderByUpdatedAtDesc(Collection<RecognitionStatus> statuses);

    boolean existsByUserIdAndLessonBlockIdAndStatusIn(Integer userId, Integer lessonBlockId, Collection<RecognitionStatus> statuses);

    @Query("""
        SELECT r FROM RecognitionRequest r
            WHERE r.user.id = :userId AND r.lessonBlock.id = :lessonBlockId
            ORDER BY r.createdAt DESC, r.id DESC
    """)
    List<RecognitionRequest> findLatestByUserAndBlock(@Param("userId") Integer userId, @Param("lessonBlockId") Integer lessonBlockId);

    @Query("""
        SELECT m.request FROM RecognitionMessage m JOIN m.files f
            WHERE f.id = :fileId
    """)
    Optional<RecognitionRequest> findByFileId(@Param("fileId") Integer fileId);
}
