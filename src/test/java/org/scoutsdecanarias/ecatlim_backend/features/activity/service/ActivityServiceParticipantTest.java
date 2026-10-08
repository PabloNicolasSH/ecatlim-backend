package org.scoutsdecanarias.ecatlim_backend.features.activity.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.scoutsdecanarias.ecatlim_backend.features.activity.dto.ForumPublicationFormDto;
import org.scoutsdecanarias.ecatlim_backend.features.activity.dto.SurveyResponseDto;
import org.scoutsdecanarias.ecatlim_backend.features.activity.entity.ActivityProgress;
import org.scoutsdecanarias.ecatlim_backend.features.activity.entity.SurveyActivity;
import org.scoutsdecanarias.ecatlim_backend.features.activity.entity.SurveyQuestion;
import org.scoutsdecanarias.ecatlim_backend.features.activity.repository.SurveyResponseRepository;
import org.scoutsdecanarias.ecatlim_backend.features.activity.entity.FileUploadActivity;
import org.scoutsdecanarias.ecatlim_backend.features.activity.entity.ForumActivity;
import org.scoutsdecanarias.ecatlim_backend.features.activity.repository.ActivityProgressRepository;
import org.scoutsdecanarias.ecatlim_backend.features.activity.repository.ActivityRepository;
import org.scoutsdecanarias.ecatlim_backend.features.activity.repository.FileSubmissionRepository;
import org.scoutsdecanarias.ecatlim_backend.features.activity.repository.ForumPublicationRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.scoutsdecanarias.ecatlim_backend.features.notification.service.NotificationService;
import org.scoutsdecanarias.ecatlim_backend.shared.blob.BlobStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceParticipantTest {

    @Mock UserRepository userRepository;
    @Mock ActivityRepository activityRepository;
    @Mock ForumPublicationRepository forumRepository;
    @Mock FileSubmissionRepository fileRepository;
    @Mock ActivityProgressRepository progressRepository;
    @Mock BlobStorageService blobStorageService;
    @Mock SurveyResponseRepository surveyResponseRepository;
    @Mock NotificationService notificationService;

    @InjectMocks ActivityService activityService;

    private User user(int id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        return user;
    }

    @Test
    void nonParticipantCannotPublishInForum() {
        when(activityRepository.findById(10)).thenReturn(Optional.of(new ForumActivity()));
        when(userRepository.findByEmail("outsider@test.com")).thenReturn(Optional.of(user(7, "outsider@test.com")));
        when(progressRepository.findByActivityIdAndStudentId(10, 7)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activityService.createForumPublication(10, "outsider@test.com", new ForumPublicationFormDto("t", "b")))
                .isInstanceOf(EcatlimException.class)
                .extracting("status").isEqualTo(HttpStatus.FORBIDDEN);
        verify(forumRepository, never()).save(any());
    }

    @Test
    void surveyMustAnswerEveryQuestionExactlyOnce() {
        SurveyActivity survey = new SurveyActivity();
        survey.setSurveyQuestions(List.of(question(1), question(2)));
        when(activityRepository.findById(10)).thenReturn(Optional.of(survey));
        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(user(7, "student@test.com")));
        when(progressRepository.findByActivityIdAndStudentId(10, 7)).thenReturn(Optional.of(new ActivityProgress()));
        when(surveyResponseRepository.findMaxAttemptByStudentAndQuestion(7, 1)).thenReturn(0);

        assertThatThrownBy(() -> activityService.submitSurveyResponses(10, "student@test.com", List.of(new SurveyResponseDto(1, "a"))))
                .isInstanceOf(EcatlimException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
        assertThatThrownBy(() -> activityService.submitSurveyResponses(10, "student@test.com",
                List.of(new SurveyResponseDto(1, "a"), new SurveyResponseDto(1, "b"))))
                .isInstanceOf(EcatlimException.class);
        verify(surveyResponseRepository, never()).save(any());
    }

    private static SurveyQuestion question(int id) {
        SurveyQuestion question = new SurveyQuestion();
        question.setId(id);
        return question;
    }

    @Test
    void nonParticipantCannotUploadAndNothingIsStored() throws Exception {
        when(activityRepository.findById(10)).thenReturn(Optional.of(new FileUploadActivity()));
        when(userRepository.findByEmail("outsider@test.com")).thenReturn(Optional.of(user(7, "outsider@test.com")));
        when(progressRepository.findByActivityIdAndStudentId(10, 7)).thenReturn(Optional.empty());
        var file = new MockMultipartFile("file", "../../user/photos/x.jpg", "image/jpeg", new byte[]{1});

        assertThatThrownBy(() -> activityService.submitFile(10, "outsider@test.com", file, null))
                .isInstanceOf(EcatlimException.class)
                .extracting("status").isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(blobStorageService);
        verify(fileRepository, never()).save(any());
    }
}
