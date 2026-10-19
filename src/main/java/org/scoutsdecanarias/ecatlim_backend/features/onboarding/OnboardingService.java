package org.scoutsdecanarias.ecatlim_backend.features.onboarding;

import lombok.RequiredArgsConstructor;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.scoutsdecanarias.ecatlim_backend.features.event.repository.EventEnrollmentRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.UserProfile;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserEducationStageRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private static final String TOUR_PREFIX = "TOUR:";
    private static final String TASK_PREFIX = "TASK:";
    private static final String BADGE_PREFIX = "BADGE:";

    private final OnboardingProgressRepository progressRepository;
    private final UserService userService;
    private final UserEducationStageRepository userEducationStageRepository;
    private final EventEnrollmentRepository eventEnrollmentRepository;

    @Transactional
    public List<OnboardingDto.ModuleDto> getProgress() {
        return buildProgress(currentUser());
    }

    @Transactional
    public List<OnboardingDto.ModuleDto> completeTour(OnboardingModule module) {
        User user = currentUser();
        if (!module.appliesTo(user.getRoles())) {
            throw new EcatlimException("El módulo " + module + " no corresponde a tus roles", HttpStatus.BAD_REQUEST);
        }
        save(user, TOUR_PREFIX + module.name());
        return buildProgress(user);
    }

    @Transactional
    public List<OnboardingDto.ModuleDto> completeTask(OnboardingTask task) {
        User user = currentUser();
        if (!task.isManual()) {
            throw new EcatlimException("La tarea " + task + " se completa automáticamente", HttpStatus.BAD_REQUEST);
        }
        boolean belongsToUserModule = List.of(OnboardingModule.values()).stream()
                .filter(module -> module.appliesTo(user.getRoles()))
                .anyMatch(module -> module.tasks(user.getRoles()).contains(task));
        if (!belongsToUserModule) {
            throw new EcatlimException("La tarea " + task + " no corresponde a tus módulos", HttpStatus.BAD_REQUEST);
        }
        save(user, TASK_PREFIX + task.name());
        return buildProgress(user);
    }

    private List<OnboardingDto.ModuleDto> buildProgress(User user) {
        Set<String> keys = new HashSet<>();
        progressRepository.findByUserId(user.getId()).forEach(progress -> keys.add(progress.getProgressKey()));

        List<OnboardingDto.ModuleDto> modules = new ArrayList<>();
        for (OnboardingModule module : OnboardingModule.values()) {
            if (!module.appliesTo(user.getRoles())) {
                continue;
            }

            List<OnboardingDto.TaskDto> tasks = module.tasks(user.getRoles()).stream()
                    .map(task -> new OnboardingDto.TaskDto(task.name(), isDone(user, task, keys), task.isManual()))
                    .toList();
            boolean tourCompleted = keys.contains(TOUR_PREFIX + module.name());
            boolean badgeAwarded = keys.contains(BADGE_PREFIX + module.name());

            boolean newlyAwarded = false;
            if (!badgeAwarded && tourCompleted && tasks.stream().allMatch(OnboardingDto.TaskDto::done)) {
                save(user, BADGE_PREFIX + module.name());
                badgeAwarded = true;
                newlyAwarded = true;
            }
            modules.add(new OnboardingDto.ModuleDto(module.name(), tourCompleted, tasks, badgeAwarded, newlyAwarded));
        }
        return modules;
    }

    private boolean isDone(User user, OnboardingTask task, Set<String> keys) {
        return switch (task) {
            case PROFILE_COMPLETED -> isProfileComplete(user.getProfile());
            case STAGE_ENROLLED -> !userEducationStageRepository.findByUserId(user.getId()).isEmpty();
            case FIRST_COURSE_ENROLLED -> eventEnrollmentRepository.existsByUserId(user.getId());
            default -> keys.contains(TASK_PREFIX + task.name());
        };
    }

    private boolean isProfileComplete(UserProfile profile) {
        return profile != null
                && hasText(profile.getName()) && hasText(profile.getSurname())
                && hasText(profile.getNif()) && hasText(profile.getPhone())
                && hasText(profile.getAddress()) && hasText(profile.getCity())
                && hasText(profile.getCountry()) && profile.getCensus() != null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private void save(User user, String key) {
        boolean exists = progressRepository.findByUserId(user.getId()).stream()
                .anyMatch(progress -> progress.getProgressKey().equals(key));
        if (exists) {
            return;
        }
        OnboardingProgress progress = new OnboardingProgress();
        progress.setUser(user);
        progress.setProgressKey(key);
        progress.setCompletedAt(LocalDateTime.now());
        progressRepository.save(progress);
    }

    private User currentUser() {
        return userService.getUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
