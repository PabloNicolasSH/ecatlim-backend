package org.scoutsdecanarias.ecatlim_backend.features.activity.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.scoutsdecanarias.ecatlim_backend.core.exception.ResourceNotFoundException;
import org.scoutsdecanarias.ecatlim_backend.features.activity.dto.ForumPublicationDto;
import org.scoutsdecanarias.ecatlim_backend.features.activity.dto.SurveyResponseDto;
import org.scoutsdecanarias.ecatlim_backend.features.activity.entity.*;
import org.scoutsdecanarias.ecatlim_backend.features.activity.enums.ActivityType;
import org.scoutsdecanarias.ecatlim_backend.features.activity.enums.EvaluationMethod;
import org.scoutsdecanarias.ecatlim_backend.features.activity.enums.ProgressStatus;
import org.scoutsdecanarias.ecatlim_backend.features.activity.repository.*;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivityService {

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final ForumPublicationRepository forumRepository;
    private final SurveyResponseRepository surveyResponseRepository;
    private final FileSubmissionRepository fileRepository;
    private final ActivityProgressRepository progressRepository;


    public List<Activity> getActivitiesByEvent(Integer eventId) {
        return activityRepository.findByEventId(eventId);
    }

    public ForumPublication createForumPublication(Integer activityId, Integer studentId, ForumPublicationDto dto) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));

        Optional<User> user = userRepository.findById(studentId);

        ForumPublication publication = new ForumPublication();
        publication.setActivity(activity);
        user.ifPresent(publication::setAuthor);
        publication.setTitle(dto.title());
        publication.setBody(dto.body());
        ForumPublication saved = forumRepository.save(publication);

        if (activity.getEvaluationMethod() == EvaluationMethod.AUTOMATIC) {
            this.completeActivity(activityId, studentId);
        }

        return saved;
    }

    public void submitSurveyResponses(Integer activityId, Integer studentId, List<SurveyResponseDto> responsesDto) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));

        Optional<User> user = userRepository.findById(studentId);

        for (SurveyResponseDto dto : responsesDto) {
            SurveyResponse response = new SurveyResponse();
            user.ifPresent(response::setStudent);
            response.setTextValue(dto.textValue());
            response.setNumValue(dto.numValue());
            surveyResponseRepository.save(response);
        }

        this.completeActivity(activityId, studentId);
    }

    public FileSubmission submitFile(Integer activityId, Integer studentId, String fileUrl, String comment) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));

        Optional<User> user = userRepository.findById(studentId);

        FileSubmission submission = new FileSubmission();
        submission.setActivity(activity);
        user.ifPresent(submission::setStudent);
        submission.setFileUrl(fileUrl);
        submission.setComment(comment);
        FileSubmission saved = fileRepository.save(submission);

        if (activity.getEvaluationMethod() == EvaluationMethod.AUTOMATIC) {
            saved.setIsApproved(true);
            this.completeActivity(activityId, studentId);
        }

        return saved;
    }

    public List<ForumPublication> getPublicationsSorted(Integer activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));

        if (activity.getActivityType() == ActivityType.GLOSSARY) {
            return forumRepository.findByActivityIdOrderByTitleAsc(activityId);
        } else {
            return forumRepository.findByActivityIdOrderByPublishedAtDesc(activityId);
        }
    }

    private void completeActivity(Integer activityId, Integer studentId) {
        ActivityProgress progress = progressRepository.findByActivityIdAndStudentId(activityId, studentId)
                .orElse(null);

        assert progress != null;

        progress.setStatus(ProgressStatus.COMPLETED);
        progress.setUpdatedAt(LocalDateTime.now());
        progressRepository.save(progress);
    }
}
