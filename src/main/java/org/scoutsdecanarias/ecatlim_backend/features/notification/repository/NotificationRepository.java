package org.scoutsdecanarias.ecatlim_backend.features.notification.repository;

import org.scoutsdecanarias.ecatlim_backend.features.notification.entity.Notification;
import org.scoutsdecanarias.ecatlim_backend.features.notification.enums.NotificationType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);

    Optional<Notification> findByIdAndUserId(Integer id, Integer userId);

    List<Notification> findByUserIdAndTypeAndReferenceIdAndResolvedAtIsNull(Integer userId, NotificationType type, Integer referenceId);

    List<Notification> findByTypeAndReferenceIdAndResolvedAtIsNull(NotificationType type, Integer referenceId);

    List<Notification> findByUserIdAndReadAtIsNull(Integer userId);

    @Query("""
            select count(n) from Notification n
            where n.user.id = :userId
              and ((n.requiresAction = true and n.resolvedAt is null)
                or (n.requiresAction = false and n.readAt is null))
            """)
    long countPending(@Param("userId") Integer userId);

    @Query("""
            select n from Notification n join fetch n.user
            where n.type <> :excluded
              and ((n.requiresAction = true and n.resolvedAt is null)
                or (n.requiresAction = false and n.readAt is null))
            order by n.createdAt
            """)
    List<Notification> findAllPendingExcludingType(@Param("excluded") NotificationType excluded);
}
