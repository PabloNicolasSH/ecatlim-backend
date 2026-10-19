package org.scoutsdecanarias.ecatlim_backend.features.onboarding;

import org.scoutsdecanarias.ecatlim_backend.features.user.enums.Role;

import java.util.List;
import java.util.Set;

public enum OnboardingModule {
    BASICS,
    STUDENT,
    HEAD_OF_EDUCATION,
    EDUCATION_TEAM,
    ADMIN;

    private static final Set<Role> EDUCATION_TEAM_ROLES =
            Set.of(Role.MANAGER_DIRECTOR, Role.MANAGEMENT, Role.EVENT_DIRECTOR, Role.TRAINER);

    public boolean appliesTo(Set<Role> roles) {
        return switch (this) {
            case BASICS -> true;
            case STUDENT -> roles.contains(Role.STUDENT);
            case HEAD_OF_EDUCATION -> roles.contains(Role.HEAD_OF_EDUCATION);
            case EDUCATION_TEAM -> roles.stream().anyMatch(EDUCATION_TEAM_ROLES::contains);
            case ADMIN -> roles.contains(Role.ADMIN);
        };
    }

    public List<OnboardingTask> tasks(Set<Role> roles) {
        return switch (this) {
            // Administrators have no personal profile to fill in.
            case BASICS -> roles.contains(Role.ADMIN) ? List.of() : List.of(OnboardingTask.PROFILE_COMPLETED);
            case STUDENT -> List.of(OnboardingTask.STAGE_ENROLLED, OnboardingTask.FIRST_COURSE_ENROLLED);
            case HEAD_OF_EDUCATION -> List.of(OnboardingTask.OPEN_PENDING_REQUESTS);
            case EDUCATION_TEAM -> List.of(OnboardingTask.OPEN_STUDENTS, OnboardingTask.OPEN_EDUCATION_OFFER);
            case ADMIN -> List.of(OnboardingTask.OPEN_USERS, OnboardingTask.OPEN_ENTITIES);
        };
    }
}
