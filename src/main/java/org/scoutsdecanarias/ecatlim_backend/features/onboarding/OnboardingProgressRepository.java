package org.scoutsdecanarias.ecatlim_backend.features.onboarding;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OnboardingProgressRepository extends JpaRepository<OnboardingProgress, Integer> {
    List<OnboardingProgress> findByUserId(Integer userId);
}
