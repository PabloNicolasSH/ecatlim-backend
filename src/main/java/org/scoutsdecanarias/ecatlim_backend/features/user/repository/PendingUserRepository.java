package org.scoutsdecanarias.ecatlim_backend.features.user.repository;

import org.scoutsdecanarias.ecatlim_backend.features.user.entity.PendingUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PendingUserRepository extends JpaRepository<PendingUser, Integer>, JpaSpecificationExecutor<PendingUser> {
    Optional<PendingUser> findByEmail(String email);
}