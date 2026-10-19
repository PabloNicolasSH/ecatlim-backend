package org.scoutsdecanarias.ecatlim_backend.features.onboarding;

import java.util.List;

public final class OnboardingDto {

    private OnboardingDto() {
    }

    public record TaskDto(String key, boolean done, boolean manual) {
    }

    /**
     * @param newlyAwarded true only in the response that awards the badge, so the client can celebrate once
     */
    public record ModuleDto(String module, boolean tourCompleted, List<TaskDto> tasks,
                            boolean badgeAwarded, boolean newlyAwarded) {
    }
}
