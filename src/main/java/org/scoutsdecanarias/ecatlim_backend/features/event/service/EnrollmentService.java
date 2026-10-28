package org.scoutsdecanarias.ecatlim_backend.features.event.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.scoutsdecanarias.ecatlim_backend.core.exception.ResourceNotFoundException;
import org.scoutsdecanarias.ecatlim_backend.features.education_stage.EducationStage;
import org.scoutsdecanarias.ecatlim_backend.features.education_stage.EducationStageRepository;
import org.scoutsdecanarias.ecatlim_backend.features.education_stage.UserEducationStage;
import org.scoutsdecanarias.ecatlim_backend.features.education_stage.dto.EducationStageCardDto;
import org.scoutsdecanarias.ecatlim_backend.features.enrollment.EnrollmentDocumentType;
import org.scoutsdecanarias.ecatlim_backend.features.enrollment.EnrollmentDocumentsDto;
import org.scoutsdecanarias.ecatlim_backend.features.enrollment.UserEnrollmentDetailDto;
import org.scoutsdecanarias.ecatlim_backend.features.event.dto.AttendedEventDto;
import org.scoutsdecanarias.ecatlim_backend.features.event.dto.StudentEnrolledEvent;
import org.scoutsdecanarias.ecatlim_backend.features.event.dto.StudentUnenrolledEvent;
import org.scoutsdecanarias.ecatlim_backend.features.event.entity.Event;
import org.scoutsdecanarias.ecatlim_backend.features.event.entity.EventConfiguration;
import org.scoutsdecanarias.ecatlim_backend.features.event.entity.EventEnrollment;
import org.scoutsdecanarias.ecatlim_backend.features.event.enums.EventStatus;
import org.scoutsdecanarias.ecatlim_backend.features.event.repository.EventEnrollmentRepository;
import org.scoutsdecanarias.ecatlim_backend.features.event.repository.EventRepository;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.BlockDetailDto;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.LessonBlock;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.LessonBlockRepository;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.UserLessonBlock;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.dto.LessonBlockCalendarSummaryDto;
import org.scoutsdecanarias.ecatlim_backend.features.module.ModuleDetailDto;
import org.scoutsdecanarias.ecatlim_backend.features.recognition.dto.RecognitionSummaryDto;
import org.scoutsdecanarias.ecatlim_backend.features.recognition.repository.RecognitionRequestRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.enums.Role;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserEducationStageRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserLessonBlockRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user_file.UserFile;
import org.scoutsdecanarias.ecatlim_backend.features.user_file.UserFileService;
import org.scoutsdecanarias.ecatlim_backend.shared.utils.FileSignatures;
import org.scoutsdecanarias.ecatlim_backend.features.user_file.UserFileType;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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
    private final RecognitionRequestRepository recognitionRepository;

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
                                                    block.getId(),
                                                    block.getCode(),
                                                    block.getName(),
                                                    progress.map(p -> p.isCompleted() ? "Superada" : "En Curso").orElse("Pendiente"),
                                                    progress.map(UserLessonBlock::getCompletionDate).orElse(null),
                                                    block.isRecognizable(),
                                                    recognitionRepository.findLatestByUserAndBlock(user.getId(), block.getId()).stream()
                                                            .findFirst().map(RecognitionSummaryDto::fromEntity).orElse(null),
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
            throw new EcatlimException("Ya estás inscrito en esta etapa", HttpStatus.CONFLICT);
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
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado"));

        validateEnrollmentOpen(event);
        if (!user.isEnabled() || !user.getRoles().contains(Role.STUDENT)) {
            throw new EcatlimException("Solo el alumnado puede inscribirse en un evento", HttpStatus.FORBIDDEN);
        }

        List<LessonBlock> blocks = new ArrayList<>();
        for (Integer lessonBlockId : new LinkedHashSet<>(lessonBlockIds)) {
            LessonBlock lessonBlock = event.getLessonBlocks().stream()
                    .filter(block -> block.getId().equals(lessonBlockId))
                    .findFirst()
                    .orElseThrow(() -> new EcatlimException("Uno de los bloques formativos no pertenece a este evento", HttpStatus.BAD_REQUEST));

            boolean canTakeBlock = userLessonBlockRepository.findByUserIdAndLessonBlockId(user.getId(), lessonBlockId)
                    .map(progress -> !progress.isCompleted())
                    .orElse(false);
            if (!canTakeBlock) {
                throw new EcatlimException("No puedes inscribirte en el bloque " + lessonBlock.getCode()
                        + ": no estás cursando su etapa o ya lo has superado", HttpStatus.FORBIDDEN);
            }
            blocks.add(lessonBlock);
        }

        for (LessonBlock lessonBlock : blocks) {
            boolean alreadyEnrolled = eventEnrollmentRepository
                    .existsByUserIdAndEventIdAndLessonBlockId(user.getId(), event.getId(), lessonBlock.getId());

            if (!alreadyEnrolled) {
                EventEnrollment enrollment = new EventEnrollment();
                enrollment.setUser(user);
                enrollment.setLessonBlock(lessonBlock);
                enrollment.setEvent(event);

                event.getEnrollments().add(enrollment);

                eventPublisher.publishEvent(new StudentEnrolledEvent(event.getId(), user.getId(), lessonBlock.getId()));
            }
        }

        return eventRepository.save(event);
    }

    @Transactional
    public Event unenrollStudent(Integer id, String userEmail, List<Integer> lessonBlockIds) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado"));

        if (event.isClosed() || event.getEndDate().isBefore(LocalDateTime.now())) {
            throw new EcatlimException("Ya no puedes desinscribirte: el plazo de inscripción ha terminado", HttpStatus.CONFLICT);
        }

        Set<Integer> blockIds = new LinkedHashSet<>(lessonBlockIds);
        for (Integer blockId : blockIds) {
            EventEnrollment enrollment = eventEnrollmentRepository
                    .findByUserIdAndEventIdAndLessonBlockId(user.getId(), event.getId(), blockId)
                    .orElseThrow(() -> new EcatlimException("No estás inscrito en uno de los bloques indicados", HttpStatus.BAD_REQUEST));
            if (enrollment.getAttendance() != null) {
                throw new EcatlimException("No puedes desinscribirte de un bloque con asistencia ya registrada", HttpStatus.CONFLICT);
            }
        }

        for (Integer blockId : blockIds) {
            // Published before the delete: assigned activities are cascade-deleted with the enrollment.
            eventPublisher.publishEvent(new StudentUnenrolledEvent(event.getId(), user.getId(), blockId));
            eventEnrollmentRepository.deleteByUserIdAndEventIdAndLessonBlockId(user.getId(), event.getId(), blockId);
        }

        event.getEnrollments().removeIf(enrollment ->
                enrollment.getUser().getId().equals(user.getId()) &&
                        blockIds.contains(enrollment.getLessonBlock().getId())
        );

        return eventRepository.save(event);
    }

    /**
     * Drops the enrollments of a deactivated user in events that have not ended, together with their tasks and
     * notifications. Past enrollments are kept as history.
     */
    @Transactional
    public void releaseUpcomingEnrollments(Integer userId) {
        releaseEnrollments(userId, eventEnrollmentRepository.findUpcomingByUserId(userId, LocalDateTime.now()));
    }

    /** Once a block is passed, any upcoming enrollment in it is pointless, so it is dropped with its tasks. */
    @Transactional
    public void releaseUpcomingEnrollmentsForBlock(Integer userId, Integer lessonBlockId) {
        releaseEnrollments(userId, eventEnrollmentRepository.findUpcomingByUserIdAndLessonBlockId(userId, lessonBlockId, LocalDateTime.now()));
    }

    private void releaseEnrollments(Integer userId, List<EventEnrollment> enrollments) {
        for (EventEnrollment enrollment : enrollments) {
            eventPublisher.publishEvent(new StudentUnenrolledEvent(
                    enrollment.getEvent().getId(), userId, enrollment.getLessonBlock().getId()));
            enrollment.getEvent().getEnrollments().remove(enrollment);
            eventEnrollmentRepository.delete(enrollment);
        }
    }

    private void validateEnrollmentOpen(Event event) {
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new EcatlimException("Este evento no está abierto a inscripciones", HttpStatus.CONFLICT);
        }
        LocalDateTime now = LocalDateTime.now();
        if (event.getEndDate().isBefore(now)) {
            throw new EcatlimException("El evento ya ha finalizado", HttpStatus.CONFLICT);
        }
        EventConfiguration config = event.getEventConfiguration();
        if (config != null && config.getDateOpenInscription() != null && now.isBefore(config.getDateOpenInscription())) {
            throw new EcatlimException("El plazo de inscripción todavía no ha abierto", HttpStatus.CONFLICT);
        }
        if (event.isClosed()) {
            throw new EcatlimException("El plazo de inscripción está cerrado", HttpStatus.CONFLICT);
        }
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
        if (file.isEmpty() || !(lowerName.endsWith(".pdf") || lowerName.endsWith(".doc") || lowerName.endsWith(".docx"))
                || !FileSignatures.contentMatchesExtension(file)) {
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
                throw new EcatlimException("Debes completar primero la etapa: " + stage.getPreviousStage().getName(), HttpStatus.FORBIDDEN);
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
