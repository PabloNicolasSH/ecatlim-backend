package org.scoutsdecanarias.ecatlim_backend.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.scoutsdecanarias.ecatlim_backend.dto.BlockDetailDto;
import org.scoutsdecanarias.ecatlim_backend.dto.EducationStageCardDto;
import org.scoutsdecanarias.ecatlim_backend.dto.UserEnrollmentDetailDto;
import org.scoutsdecanarias.ecatlim_backend.entity.EducationStage;
import org.scoutsdecanarias.ecatlim_backend.entity.User;
import org.scoutsdecanarias.ecatlim_backend.entity.UserEducationStage;
import org.scoutsdecanarias.ecatlim_backend.entity.UserLessonBlock;
import org.scoutsdecanarias.ecatlim_backend.repository.EducationStageRepository;
import org.scoutsdecanarias.ecatlim_backend.repository.LessonBlockRepository;
import org.scoutsdecanarias.ecatlim_backend.repository.UserEducationStageRepository;
import org.scoutsdecanarias.ecatlim_backend.repository.UserLessonBlockRepository;
import org.scoutsdecanarias.ecatlim_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final UserEducationStageRepository userStageRepository;
    private final UserRepository userRepository;
    private final EducationStageRepository stageRepository;
    private final UserLessonBlockRepository userLessonBlockRepository;
    private final LessonBlockRepository lessonBlockRepository;

    public List<UserEnrollmentDetailDto> getUserProgress(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        return userStageRepository.findByUserId(user.getId()).stream()
                .map(enrollment -> {
                    EducationStage stage = enrollment.getEducationStage();

                    List<BlockDetailDto> blocks = stage.getModules().stream()
                            .flatMap(m -> m.getLessonBlocks().stream())
                            .map(block -> {
                                Optional<UserLessonBlock> progress = userLessonBlockRepository
                                        .findByUserIdAndLessonBlockId(user.getId(), block.getId());

                                return new BlockDetailDto(
                                        block.getCode(),
                                        block.getName(),
                                        progress.map(p -> p.isCompleted() ? "Superada" : "En Curso").orElse("Pendiente"),
                                        progress.map(UserLessonBlock::getCompletionDate).orElse(null),
                                        //TODO: Add activities with user story ECL-11
                                        Collections.emptyList()
                                );
                            }).toList();

                    return new UserEnrollmentDetailDto(
                            enrollment.getId(),
                            stage.getName(),
                            "COMPLETED".equals(enrollment.getStatus().toString()),
                            this.calculateProgress(user.getId(), stage.getId()),
                            blocks
                    );
                }).toList();
    }

    @Transactional
    public EducationStageCardDto enrollUserInStage(String userEmail, Integer stageId) {
        EducationStage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new EntityNotFoundException("Etapa no encontrada"));

        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        validateHierarchyRequirements(user, stage);

        if (userStageRepository.existsByUserIdAndEducationStageId(user.getId(), stageId)) {
            throw new IllegalStateException("Ya estás inscrito en esta etapa");
        }

        UserEducationStage enrollment = new UserEducationStage();
        enrollment.setUser(user);
        enrollment.setEducationStage(stage);
        enrollment.setStatus(UserEducationStage.StageStatus.ENROLLED);
        enrollment.setEnrollmentDate(new Date());

        userStageRepository.save(enrollment);

        return convertToCardDto(stage, "ENROLLED", true);
    }

    private void validateHierarchyRequirements(User user, EducationStage stage) {
        if (stage.getPreviousStage() != null) {
            boolean previousDone = userStageRepository.findByUserId(user.getId()).stream()
                    .anyMatch(ue -> ue.getEducationStage().getId().equals(stage.getPreviousStage().getId())
                            && UserEducationStage.StageStatus.COMPLETED.equals(ue.getStatus()));

            if (!previousDone) {
                throw new SecurityException("Debes completar primero la etapa: " + stage.getPreviousStage().getName());
            }
        }
    }

    public int calculateProgress(Integer userId, Integer stageId) {
        int total = lessonBlockRepository.countByStageId(stageId);
        int completed = userLessonBlockRepository.countCompletedByUserIdAndStageId(userId, stageId);

        return (total > 0) ? (completed * 100) / total : 0;
    }

    private EducationStageCardDto convertToCardDto(EducationStage stage, String status, boolean isEnabled) {
        return new EducationStageCardDto(
                stage.getId(),
                stage.getName(),
                stage.getDescription(),
                stage.getCode(),
                status,
                isEnabled
        );
    }
}
