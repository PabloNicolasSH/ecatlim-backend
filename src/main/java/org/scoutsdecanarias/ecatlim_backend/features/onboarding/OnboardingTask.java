package org.scoutsdecanarias.ecatlim_backend.features.onboarding;

/**
 * Tasks that complete an onboarding module. Automatic tasks are derived from the user's data,
 * manual tasks are reported by the frontend when the user does what the task asks for.
 */
public enum OnboardingTask {
    PROFILE_COMPLETED(false),
    STAGE_ENROLLED(false),
    FIRST_COURSE_ENROLLED(false),
    OPEN_PENDING_REQUESTS(true),
    OPEN_STUDENTS(true),
    OPEN_EDUCATION_OFFER(true),
    OPEN_USERS(true),
    OPEN_ENTITIES(true);

    private final boolean manual;

    OnboardingTask(boolean manual) {
        this.manual = manual;
    }

    public boolean isManual() {
        return manual;
    }
}
