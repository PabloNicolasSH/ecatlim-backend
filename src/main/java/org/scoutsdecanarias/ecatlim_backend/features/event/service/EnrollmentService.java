package org.scoutsdecanarias.ecatlim_backend.features.event.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.scoutsdecanarias.ecatlim_backend.features.education_stage.EducationStage;
import org.scoutsdecanarias.ecatlim_backend.features.education_stage.EducationStageRepository;
import org.scoutsdecanarias.ecatlim_backend.features.education_stage.UserEducationStage;
import org.scoutsdecanarias.ecatlim_backend.features.education_stage.dto.EducationStageCardDto;
import org.scoutsdecanarias.ecatlim_backend.features.enrollment.EnrollmentDocumentType;
import org.scoutsdecanarias.ecatlim_backend.features.enrollment.EnrollmentDocumentsDto;
import org.scoutsdecanarias.ecatlim_backend.features.enrollment.UserEnrollmentDetailDto;
import org.scoutsdecanarias.ecatlim_backend.features.event.dto.AttendedEventDto;
import org.scoutsdecanarias.ecatlim_backend.features.event.dto.StudentEnrolledEvent;
import org.scoutsdecanarias.ecatlim_backend.features.event.entity.Event;
import org.scoutsdecanarias.ecatlim_backend.features.event.entity.EventEnrollment;
import org.scoutsdecanarias.ecatlim_backend.features.event.repository.EventEnrollmentRepository;
import org.scoutsdecanarias.ecatlim_backend.features.event.repository.EventRepository;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.BlockDetailDto;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.LessonBlock;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.LessonBlockRepository;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.UserLessonBlock;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.dto.LessonBlockCalendarSummaryDto;
import org.scoutsdecanarias.ecatlim_backend.features.module.ModuleDetailDto;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserEducationStageRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserLessonBlockRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user_file.UserFile;
import org.scoutsdecanarias.ecatlim_backend.features.user_file.UserFileService;
import org.scoutsdecanarias.ecatlim_backend.features.user_file.UserFileType;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final UserEducationStageRepository userStageRepository;
    private final UserRepository userRepository;
    private final EducationStageRepository stageRepository;
    private final UserLessonBlockRepository userLessonBlockRepository;
    private final LessonBlockRepository lessonBlockRepository;
    private final EventEnrollmentRepository eventEnrollmentRepository;
    private final EventRepository eventRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserFileService userFileService;

    public List<UserEnrollmentDetailDto> getUserProgress(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        return userStageRepository.findByUserId(user.getId()).stream()
                .map(enrollment -> {
                    EducationStage stage = enrollment.getEducationStage();

                    List<ModuleDetailDto> modules = stage.getModules().stream()
                            .map(module -> {
                                List<BlockDetailDto> blocks = module.getLessonBlocks().stream()
                                        .map(block -> {
                                            Optional<UserLessonBlock> progress = userLessonBlockRepository
                                                    .findByUserIdAndLessonBlockId(user.getId(), block.getId());

                                            return new BlockDetailDto(
                                                    block.getCode(),
                                                    block.getName(),
                                                    progress.map(p -> p.isCompleted() ? "Superada" : "En Curso").orElse("Pendiente"),
                                                    progress.map(UserLessonBlock::getCompletionDate).orElse(null),
                                                    // TODO: Add activities
                                                    Collections.emptyList()
                                            );
                                        }).toList();

                                return new ModuleDetailDto(
                                        module.getName(),
                                        module.getCode(),
                                        blocks
                                );
                            }).toList();

                    return new UserEnrollmentDetailDto(
                            enrollment.getId(),
                            stage.getName(),
                            "COMPLETED".equals(enrollment.getStatus().toString()),
                            this.calculateProgress(user.getId(), stage.getId()),
                            modules,
                            EnrollmentDocumentsDto.fromEnrollment(enrollment)
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

        List<UserLessonBlock> userBlocks = stage.getModules().stream()
                .flatMap(module -> module.getLessonBlocks().stream())
                .map(block -> {
                    UserLessonBlock ulb = new UserLessonBlock();
                    ulb.setUser(user);
                    ulb.setLessonBlock(block);
                    return ulb;
                })
                .toList();

        if (!userBlocks.isEmpty()) {
            userLessonBlockRepository.saveAll(userBlocks);
        }

        return convertToCardDto(stage, "ENROLLED", true);
    }

    @Transactional
    public Event enrollStudent(Integer id, String userEmail, List<Integer> lessonBlockIds) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        Event event = eventRepository.findById(id).orElseThrow();

        for (Integer lessonBlockId : lessonBlockIds) {
            LessonBlock lessonBlock = lessonBlockRepository.findById(lessonBlockId).orElseThrow();

            boolean alreadyEnrolled = eventEnrollmentRepository
                    .existsByUserIdAndEventIdAndLessonBlockId(user.getId(), event.getId(), lessonBlockId);

            if (!alreadyEnrolled) {
                EventEnrollment enrollment = new EventEnrollment();
                enrollment.setUser(user);
                enrollment.setLessonBlock(lessonBlock);
                enrollment.setEvent(event);

                event.getEnrollments().add(enrollment);

                eventPublisher.publishEvent(new StudentEnrolledEvent(event.getId(), user.getId(), lessonBlockId));
            }
        }

        return eventRepository.save(event);
    }

    @Transactional
    public Event unenrollStudent(Integer id, String userEmail, List<Integer> lessonBlockIds) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        Event event = eventRepository.findById(id).orElseThrow();

        for (Integer blockId : lessonBlockIds) {
            eventEnrollmentRepository.deleteByUserIdAndEventIdAndLessonBlockId(user.getId(), event.getId(), blockId);
        }

        event.getEnrollments().removeIf(enrollment ->
                enrollment.getUser().getId().equals(user.getId()) &&
                        lessonBlockIds.contains(enrollment.getLessonBlock().getId())
        );

        return eventRepository.save(event);
    }

    @Transactional
    public List<AttendedEventDto> getAttendedEvents(String userEmail, Integer stageId) {
        Map<Event, List<EventEnrollment>> byEvent = eventEnrollmentRepository
                .findAttendedByUserEmailAndStageId(userEmail, stageId).stream()
                .collect(Collectors.groupingBy(EventEnrollment::getEvent, LinkedHashMap::new, Collectors.toList()));

        return byEvent.entrySet().stream()
                .map(entry -> {
                    Event event = entry.getKey();
                    List<LessonBlockCalendarSummaryDto> blocks = entry.getValue().stream()
                            .map(ee -> LessonBlockCalendarSummaryDto.fromEntity(ee.getLessonBlock()))
                            .sorted(Comparator.comparing(LessonBlockCalendarSummaryDto::code))
                            .toList();
                    return new AttendedEventDto(event.getId(), event.getTitle(), event.getStartDate(),
                            event.getEndDate(), event.getLocation(), blocks);
                })
                .toList();
    }

    @Transactional
    public EnrollmentDocumentsDto uploadDocument(String userEmail, Integer enrollmentId, EnrollmentDocumentType type, MultipartFile file) {
        UserEducationStage enrollment = userStageRepository.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException("Inscripción no encontrada"));
        if (!enrollment.getUser().getEmail().equals(userEmail)) {
            throw new EcatlimException("No puedes modificar esta inscripción", HttpStatus.FORBIDDEN);
        }

        String lowerName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (file.isEmpty() || !(lowerName.endsWith(".pdf") || lowerName.endsWith(".doc") || lowerName.endsWith(".docx"))) {
            throw new EcatlimException("El archivo debe ser un PDF o un documento Word", HttpStatus.BAD_REQUEST);
        }

        UserFile previous = type == EnrollmentDocumentType.PERSONAL_PLAN ? enrollment.getPersonalPlan() : enrollment.getEntityApproval();
        UserFile stored = userFileService.storeFile(file, UserFileType.USER_EDUCATION_STAGE, type.name().toLowerCase());

        if (type == EnrollmentDocumentType.PERSONAL_PLAN) {
            enrollment.setPersonalPlan(stored);
        } else {
            enrollment.setEntityApproval(stored);
        }
        userStageRepository.save(enrollment);

        if (previous != null) {
            userFileService.deleteStoredFile(previous);
        }
        return EnrollmentDocumentsDto.fromEnrollment(enrollment);
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
