package org.scoutsdecanarias.ecatlim_backend.features.activity.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.scoutsdecanarias.ecatlim_backend.core.exception.ResourceNotFoundException;
import org.scoutsdecanarias.ecatlim_backend.features.activity.dto.*;
import org.scoutsdecanarias.ecatlim_backend.features.activity.entity.*;
import org.scoutsdecanarias.ecatlim_backend.features.activity.enums.ActivityType;
import org.scoutsdecanarias.ecatlim_backend.features.activity.enums.EvaluationMethod;
import org.scoutsdecanarias.ecatlim_backend.features.activity.enums.ProgressStatus;
import org.scoutsdecanarias.ecatlim_backend.features.activity.enums.SurveyResponseType;
import org.scoutsdecanarias.ecatlim_backend.features.activity.repository.*;
import org.scoutsdecanarias.ecatlim_backend.features.event.dto.StudentEnrolledEvent;
import org.scoutsdecanarias.ecatlim_backend.features.event.entity.Event;
import org.scoutsdecanarias.ecatlim_backend.features.event.repository.EventEnrollmentRepository;
import org.scoutsdecanarias.ecatlim_backend.features.event.repository.EventRepository;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.LessonBlock;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.LessonBlockRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.enums.Role;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.scoutsdecanarias.ecatlim_backend.shared.blob.BlobDirectory;
import org.scoutsdecanarias.ecatlim_backend.shared.blob.BlobStorageService;
import org.scoutsdecanarias.ecatlim_backend.shared.utils.FileNames;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final EventRepository eventRepository;
    private final SurveyOptionRepository surveyOptionRepository;
    private final SurveyQuestionRepository surveyQuestionRepository;
    private final LessonBlockRepository lessonBlockRepository;
    private final EventEnrollmentRepository eventEnrollmentRepository;
    private final BlobStorageService blobStorageService;

    public List<ActivityDto> getActivitiesByEventForUser(Integer eventId, String userEmail) {
        Integer userId = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail)).getId();
        return activityRepository.findByEventId(eventId).stream()
                .map(activity -> ActivityDto.fromEntity(activity).withProgressStatus(
                        progressRepository.findByActivityIdAndStudentId(activity.getId(), userId)
                                .map(p -> p.getStatus().name())
                                .orElse(ProgressStatus.PENDING.name())))
                .toList();
    }

    public List<Activity> getActivitiesByEvent(Integer eventId) {
        return activityRepository.findByEventId(eventId);
    }

    public Activity createActivity(Integer eventId, ActivityCreationDto dto, String userEmail) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + eventId));

        User creator = userRepository.findByEmail(userEmail).orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        LessonBlock lessonBlock = lessonBlockRepository.findById(dto.lessonBlockId()).orElseThrow(() -> new ResourceNotFoundException("Lesson Block not found with id: " + dto.lessonBlockId()));

        if (dto.responsibleId() == null) {
            throw new EcatlimException("Debes indicar el formador responsable de la actividad", HttpStatus.BAD_REQUEST);
        }
        User responsible = userRepository.findById(dto.responsibleId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.responsibleId()));
        if (!responsible.getRoles().contains(Role.TRAINER)) {
            throw new EcatlimException("El responsable de la actividad debe tener el rol de formador", HttpStatus.BAD_REQUEST);
        }

        Activity activity;
        ActivityType activityType = ActivityType.valueOf(dto.activityType());

        switch (activityType) {
            case FORUM, GLOSSARY -> activity = new ForumActivity();
            case FILE_UPLOAD ->  activity = new FileUploadActivity();
            case SURVEY -> {
                SurveyActivity surveyActivity = new SurveyActivity();
                surveyActivity.setIsGradable(dto.isGradable());
                surveyActivity.setMaxAttempts(dto.maxAttempts());
                surveyActivity.setPassingScore(dto.passingScore());
                activity = surveyActivity;
            }
            default ->  throw new IllegalArgumentException("Invalid activity type");
        }

        activity.setEvent(event);
        activity.setCreator(creator);
        activity.setLessonBlock(lessonBlock);
        activity.getCorrectors().add(responsible);

        activity.setTitle(dto.title());
        activity.setDescription(dto.description());
        activity.setActivityType(activityType);
        activity.setEvaluationMethod(EvaluationMethod.valueOf(dto.evaluationMethod()));
        activity.setAvailableAt(dto.availableAt());
        activity.setDueDate(dto.dueDate());
        activity.setCreatedAt(LocalDateTime.now());
        activity.setIsOptional(dto.isOptional() != null ? dto.isOptional() : false);

        Activity savedActivity = activityRepository.save(activity);

        if (activityType == ActivityType.SURVEY && dto.questions() != null && savedActivity instanceof SurveyActivity surveyActivity) {
            for (QuestionCreationDto qDto : dto.questions()) {
                SurveyQuestion question = new SurveyQuestion();
                question.setActivity(surveyActivity);
                question.setQuestionText(qDto.questionText());
                question.setResponseType(SurveyResponseType.valueOf(qDto.responseType()));
                SurveyQuestion savedQuestion = surveyQuestionRepository.save(question);

                if (qDto.options() != null) {
                    for (OptionCreationDto oDto : qDto.options()) {
                        SurveyOption option = new SurveyOption();
                        option.setQuestion(savedQuestion);
                        option.setOptionText(oDto.optionText());
                        option.setCorrect(oDto.isCorrect() != null ? oDto.isCorrect() : false);
                        surveyOptionRepository.save(option);
                    }
                }
            }
        }

        generateInitialProgressForActivity(savedActivity, eventId);

        return savedActivity;
    }

    private void generateInitialProgressForActivity(Activity activity, Integer eventId) {
        List<Integer> targetStudentIds = eventEnrollmentRepository.findStudentIdsByEventAndLessonBlock(
                eventId,
                activity.getLessonBlock().getId()
        );

        List<ActivityProgress> initialProgresses = targetStudentIds.stream().map(studentId -> {
            ActivityProgress progress = new ActivityProgress();
            progress.setActivity(activity);
            progress.setStudentId(studentId);
            progress.setStatus(ProgressStatus.PENDING);
            return progress;
        }).toList();

        progressRepository.saveAll(initialProgresses);
    }

    public ForumPublication createForumPublication(Integer activityId, String userEmail, ForumPublicationFormDto dto) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));

        if (!(activity instanceof ForumActivity forumActivity)) {
            throw new EcatlimException("Esta actividad no es un foro ni un glosario", HttpStatus.BAD_REQUEST);
        }

        User student = getParticipant(activityId, userEmail);

        ForumPublication publication = new ForumPublication();
        publication.setActivity(forumActivity);
        publication.setAuthor(student);
        publication.setTitle(dto.title());
        publication.setBody(dto.body());
        ForumPublication saved = forumRepository.save(publication);

        if (activity.getEvaluationMethod() == EvaluationMethod.AUTOMATIC) {
            this.completeActivity(activityId, student.getId());
        }

        return saved;
    }

    public void submitSurveyResponses(Integer activityId, String userEmail, List<SurveyResponseDto> responsesDto) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));

        if (!(activity instanceof SurveyActivity surveyActivity)) {
            throw new EcatlimException("Esta actividad no es una encuesta ni un examen", HttpStatus.BAD_REQUEST);
        }

        User student = getParticipant(activityId, userEmail);
        Integer studentId = student.getId();
        Optional<User> user = Optional.of(student);

        ActivityProgress progress = progressRepository.findByActivityIdAndStudentId(activityId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Progress record not found"));

        Integer attempts = 0;
        if (!surveyActivity.getSurveyQuestions().isEmpty()) {
            Integer firstQuestionId = surveyActivity.getSurveyQuestions().getFirst().getId();
            attempts = surveyResponseRepository.findMaxAttemptByStudentAndQuestion(studentId, firstQuestionId);
        }

        if (Boolean.TRUE.equals(surveyActivity.getIsGradable()) && surveyActivity.getMaxAttempts() != null
                && attempts >= surveyActivity.getMaxAttempts()) {
            throw new EcatlimException("Has alcanzado el número máximo de intentos de este examen", HttpStatus.CONFLICT);
        }

        Set<Integer> questionIds = surveyActivity.getSurveyQuestions().stream().map(SurveyQuestion::getId).collect(Collectors.toSet());
        List<Integer> answeredIds = responsesDto.stream().map(SurveyResponseDto::id).toList();
        if (answeredIds.size() != questionIds.size() || !questionIds.equals(new HashSet<>(answeredIds))) {
            throw new EcatlimException("Debes responder a todas las preguntas de la encuesta, una sola vez cada una", HttpStatus.BAD_REQUEST);
        }

        int correctAnswersCount = 0;
        int totalGradableQuestions = 0;

        LocalDateTime responsedTime = LocalDateTime.now();

        for (SurveyResponseDto dto : responsesDto) {
            SurveyQuestion question = surveyQuestionRepository.findById(dto.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
            if (!question.getActivity().getId().equals(activityId)) {
                throw new EcatlimException("La pregunta no pertenece a esta actividad", HttpStatus.BAD_REQUEST);
            }

            SurveyResponse response = new SurveyResponse();
            user.ifPresent(response::setStudent);
            response.setQuestion(question);
            response.setResponseValue(dto.responseValue());
            response.setSubmittedAt(responsedTime);
            response.setAttemptNumber(attempts + 1);
            surveyResponseRepository.save(response);

            if (Boolean.TRUE.equals(surveyActivity.getIsGradable()) && surveyActivity.getMaxAttempts() != null) {
                totalGradableQuestions++;
                boolean isCorrectChoice = question.getOptions().stream()
                        .filter(SurveyOption::isCorrect)
                        .anyMatch(option -> option.getOptionText().equals(dto.responseValue()));

                if (isCorrectChoice) {
                    correctAnswersCount++;
                }
            }
        }

        progress.setUpdatedAt(LocalDateTime.now());

        if (Boolean.TRUE.equals(surveyActivity.getIsGradable()) && totalGradableQuestions > 0) {
            double finalScore = ((double) correctAnswersCount / totalGradableQuestions) * 10.0;
            progress.setScore(finalScore);

            if (finalScore >= surveyActivity.getPassingScore()) {
                progress.setStatus(ProgressStatus.COMPLETED);
            } else {
                progress.setStatus(ProgressStatus.PENDING);
            }
        } else {
            progress.setStatus(ProgressStatus.COMPLETED);
        }

        progressRepository.save(progress);
    }

    public FileSubmission submitFile(Integer activityId, String userEmail, MultipartFile file, String comment) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));

        if (!(activity instanceof FileUploadActivity fileUploadActivity)) {
            throw new EcatlimException("Esta actividad no admite entregas de archivos", HttpStatus.BAD_REQUEST);
        }
        if (file == null || file.isEmpty()) {
            throw new EcatlimException("Debes adjuntar un archivo", HttpStatus.BAD_REQUEST);
        }

        User student = getParticipant(activityId, userEmail);

        String blobName = student.getId() + "_" + System.currentTimeMillis() + "_" + FileNames.randomBlobName(file.getOriginalFilename());
        String fileUrl;
        try {
            fileUrl = blobStorageService.upload(file, BlobDirectory.ACTIVITY_ATTACHMENTS, blobName).url();
        } catch (IOException e) {
            throw new EcatlimException("No se ha podido guardar el archivo, inténtalo de nuevo", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        FileSubmission submission = new FileSubmission();
        submission.setActivity(fileUploadActivity);
        submission.setStudent(student);
        submission.setFileUrl(fileUrl);
        submission.setComment(comment);
        FileSubmission saved = fileRepository.save(submission);

        if (fileUploadActivity.getEvaluationMethod() == EvaluationMethod.AUTOMATIC) {
            saved.setIsApproved(true);
            this.completeActivity(activityId, student.getId());
        }

        return saved;
    }

    private User getParticipant(Integer activityId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
        if (progressRepository.findByActivityIdAndStudentId(activityId, user.getId()).isEmpty()) {
            throw new EcatlimException("No estás inscrito en el bloque de esta actividad", HttpStatus.FORBIDDEN);
        }
        return user;
    }

    @EventListener
    @Transactional
    public void handleStudentEnrolled(StudentEnrolledEvent event) {
        List<Activity> blockActivities = activityRepository.findByEventIdAndLessonBlockId(
                event.eventId(),
                event.lessonBlockId()
        );

        List<ActivityProgress> progressesForNewStudent = blockActivities.stream().map(activity -> {
            ActivityProgress progress = new ActivityProgress();
            progress.setActivity(activity);
            progress.setStudentId(event.studentId());
            progress.setStatus(ProgressStatus.PENDING);
            return progress;
        }).toList();

        progressRepository.saveAll(progressesForNewStudent);
    }

    public List<ForumPublication> getPublicationsSorted(Integer activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));

        if (!(activity instanceof ForumActivity)) {
            throw new IllegalArgumentException("This activity is not a Forum/Glossary");
        }

        if (activity.getActivityType() == ActivityType.GLOSSARY) {
            return forumRepository.findByActivityIdOrderByTitleAsc(activityId);
        } else {
            return forumRepository.findByActivityIdOrderByPublishedAtDesc(activityId);
        }
    }

    private void completeActivity(Integer activityId, Integer studentId) {
        ActivityProgress progress = progressRepository.findByActivityIdAndStudentId(activityId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Progress record not found"));

        progress.setStatus(ProgressStatus.COMPLETED);
        progress.setUpdatedAt(LocalDateTime.now());
        progressRepository.save(progress);
    }
}
