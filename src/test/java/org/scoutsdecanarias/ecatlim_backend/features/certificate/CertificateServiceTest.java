package org.scoutsdecanarias.ecatlim_backend.features.certificate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.scoutsdecanarias.ecatlim_backend.features.education_stage.EducationStage;
import org.scoutsdecanarias.ecatlim_backend.features.education_stage.UserEducationStage;
import org.scoutsdecanarias.ecatlim_backend.features.event.service.EnrollmentService;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.LessonBlock;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.UserLessonBlock;
import org.scoutsdecanarias.ecatlim_backend.features.module.Module;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserEducationStageRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserLessonBlockRepository;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CertificateServiceTest {

    @Mock UserLessonBlockRepository userLessonBlockRepository;
    @Mock UserEducationStageRepository userStageRepository;
    @Mock EnrollmentService enrollmentService;

    @InjectMocks CertificateService certificateService;

    private UserLessonBlock progress(boolean completed) {
        EducationStage stage = new EducationStage();
        stage.setId(4);
        Module module = new Module();
        module.setEducationStage(stage);
        LessonBlock block = new LessonBlock();
        block.setId(8);
        block.setModule(module);
        UserLessonBlock progress = new UserLessonBlock();
        progress.setLessonBlock(block);
        progress.setCompleted(completed);
        when(userLessonBlockRepository.findByUserIdAndLessonBlockId(7, 8)).thenReturn(Optional.of(progress));
        return progress;
    }

    private UserEducationStage stageEnrollment(UserEducationStage.StageStatus status) {
        EducationStage stage = new EducationStage();
        stage.setId(4);
        UserEducationStage enrollment = new UserEducationStage();
        enrollment.setEducationStage(stage);
        enrollment.setStatus(status);
        return enrollment;
    }

    @Test
    void passingABlockDropsItsUpcomingEnrollments() {
        progress(false);

        certificateService.setBlockPassed(7, 8, true);

        verify(enrollmentService).releaseUpcomingEnrollmentsForBlock(7, 8);
    }

    @Test
    void cannotUnpassABlockOfACompletedStage() {
        UserLessonBlock progress = progress(true);
        when(userStageRepository.findByUserId(7)).thenReturn(List.of(stageEnrollment(UserEducationStage.StageStatus.COMPLETED)));

        assertThatThrownBy(() -> certificateService.setBlockPassed(7, 8, false))
                .isInstanceOf(EcatlimException.class)
                .extracting("status").isEqualTo(HttpStatus.CONFLICT);
        assertThat(progress.isCompleted()).isTrue();
        verify(userLessonBlockRepository, never()).save(any());
    }

    @Test
    void canUnpassABlockWhileTheStageIsStillOpen() {
        UserLessonBlock progress = progress(true);
        when(userStageRepository.findByUserId(7)).thenReturn(List.of(stageEnrollment(UserEducationStage.StageStatus.ENROLLED)));

        certificateService.setBlockPassed(7, 8, false);

        assertThat(progress.isCompleted()).isFalse();
    }

    @Test
    void aStageCannotBeCompletedTwice() {
        UserEducationStage enrollment = stageEnrollment(UserEducationStage.StageStatus.COMPLETED);
        when(userStageRepository.findById(3)).thenReturn(Optional.of(enrollment));

        assertThatThrownBy(() -> certificateService.completeStage(3))
                .isInstanceOf(EcatlimException.class)
                .extracting("status").isEqualTo(HttpStatus.CONFLICT);
    }
}
