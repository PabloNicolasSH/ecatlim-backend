package org.scoutsdecanarias.ecatlim_backend.features.onboarding;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    @GetMapping("/me")
    public List<OnboardingDto.ModuleDto> getProgress() {
        return onboardingService.getProgress();
    }

    @PutMapping("/me/modules/{module}/tour-completed")
    public List<OnboardingDto.ModuleDto> completeTour(@PathVariable OnboardingModule module) {
        return onboardingService.completeTour(module);
    }

    @PutMapping("/me/tasks/{task}")
    public List<OnboardingDto.ModuleDto> completeTask(@PathVariable OnboardingTask task) {
        return onboardingService.completeTask(task);
    }
}
