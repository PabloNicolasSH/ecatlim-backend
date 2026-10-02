package org.scoutsdecanarias.ecatlim_backend.features.user.repository;

import org.scoutsdecanarias.ecatlim_backend.features.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {
    Optional<UserProfile> findByNif(String nif);
}
