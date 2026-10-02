package org.scoutsdecanarias.ecatlim_backend.features.user.service;

import lombok.RequiredArgsConstructor;
import org.scoutsdecanarias.ecatlim_backend.features.education_stage.EducationStage;
import org.scoutsdecanarias.ecatlim_backend.features.education_stage.EducationStageRepository;
import org.scoutsdecanarias.ecatlim_backend.features.education_stage.UserEducationStage;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.SimpleUserDto;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.StudentOverviewDto;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.enums.Role;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class StudentOverviewService {

    private static final Set<UserEducationStage.StageStatus> ACTIVE_STATUSES =
            Set.of(UserEducationStage.StageStatus.ENROLLED, UserEducationStage.StageStatus.IN_PROGRESS);

    private final UserRepository userRepository;
    private final EducationStageRepository educationStageRepository;

    @Transactional(readOnly = true)
    public StudentOverviewDto getOverview() {
        List<StudentOverviewDto.StudentDto> students = userRepository.findAllByRolesContaining(Role.STUDENT).stream()
                .filter(User::isEnabled)
                .map(this::toStudentDto)
                .sorted(Comparator.comparing((StudentOverviewDto.StudentDto s) -> s.surname() == null ? "" : s.surname().toLowerCase())
                        .thenComparing(s -> s.name() == null ? "" : s.name().toLowerCase()))
                .toList();

        Map<Integer, Integer> countByStage = new HashMap<>();
        int withoutStage = 0;
        for (StudentOverviewDto.StudentDto student : students) {
            if (student.currentStage() == null) {
                withoutStage++;
            } else {
                countByStage.merge(student.currentStage().id(), 1, Integer::sum);
            }
        }

        List<StudentOverviewDto.StageCountDto> stages = educationStageRepository.findAll().stream()
                .sorted(Comparator.comparing(EducationStage::getId))
                .map(stage -> new StudentOverviewDto.StageCountDto(
                        stage.getId(), stage.getName(), stage.getCode(), countByStage.getOrDefault(stage.getId(), 0)))
                .toList();

        return new StudentOverviewDto(students, stages, withoutStage);
    }

    private StudentOverviewDto.StudentDto toStudentDto(User user) {
        SimpleUserDto simple = SimpleUserDto.fromEntity(user);
        boolean hasGroup = user.getProfile() != null && user.getProfile().getScoutGroup() != null;

        Optional<UserEducationStage> current = user.getEducationStages() == null ? Optional.empty()
                : user.getEducationStages().stream()
                .filter(enrollment -> enrollment.getStatus() != null && ACTIVE_STATUSES.contains(enrollment.getStatus()))
                .max(Comparator.comparing(UserEducationStage::getEnrollmentDate,
                        Comparator.nullsFirst(Comparator.naturalOrder())));

        return new StudentOverviewDto.StudentDto(
                user.getId(),
                simple.name(),
                simple.surname(),
                user.getEmail(),
                simple.avatarUrl(),
                hasGroup ? user.getProfile().getScoutGroup().getName() : null,
                current.map(enrollment -> new StudentOverviewDto.CurrentStageDto(
                        enrollment.getEducationStage().getId(),
                        enrollment.getEducationStage().getName(),
                        enrollment.getEducationStage().getCode(),
                        enrollment.getStatus().name())).orElse(null)
        );
    }
}
