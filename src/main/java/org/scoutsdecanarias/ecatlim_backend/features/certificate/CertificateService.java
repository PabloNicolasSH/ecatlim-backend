package org.scoutsdecanarias.ecatlim_backend.features.certificate;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.scoutsdecanarias.ecatlim_backend.core.exception.ResourceNotFoundException;
import org.scoutsdecanarias.ecatlim_backend.features.certificate.dto.AttendanceSummaryDto;
import org.scoutsdecanarias.ecatlim_backend.features.education_stage.UserEducationStage;
import org.scoutsdecanarias.ecatlim_backend.features.enrollment.EnrollmentDocumentsDto;
import org.scoutsdecanarias.ecatlim_backend.features.enrollment.UserEnrollmentDetailDto;
import org.scoutsdecanarias.ecatlim_backend.features.event.entity.Event;
import org.scoutsdecanarias.ecatlim_backend.features.event.entity.EventEnrollment;
import org.scoutsdecanarias.ecatlim_backend.features.event.repository.EventEnrollmentRepository;
import org.scoutsdecanarias.ecatlim_backend.features.event.service.EnrollmentService;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.LessonBlock;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.LessonBlockRepository;
import org.scoutsdecanarias.ecatlim_backend.features.notification.service.NotificationService;
import org.scoutsdecanarias.ecatlim_backend.features.notification.enums.NotificationType;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.UserLessonBlock;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserEducationStageRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserLessonBlockRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user_file.UserFile;
import org.scoutsdecanarias.ecatlim_backend.features.user_file.UserFileService;
import org.scoutsdecanarias.ecatlim_backend.features.user_file.UserFileType;
import org.scoutsdecanarias.ecatlim_backend.shared.email.EmailAttachment;
import org.scoutsdecanarias.ecatlim_backend.shared.email.EmailService;
import org.scoutsdecanarias.ecatlim_backend.shared.utils.FileTransferDto;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateService {

    private static final String PDF = "application/pdf";

    private final UserRepository userRepository;
    private final EventEnrollmentRepository eventEnrollmentRepository;
    private final UserLessonBlockRepository userLessonBlockRepository;
    private final LessonBlockRepository lessonBlockRepository;
    private final UserEducationStageRepository userStageRepository;
    private final UserFileService userFileService;
    private final CertificatePdfService pdfService;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final EnrollmentService enrollmentService;

    @Transactional
    public List<UserEnrollmentDetailDto> getUserProgress(Integer userId) {
        return enrollmentService.getUserProgress(findUser(userId).getEmail());
    }

    @Transactional
    public AttendanceSummaryDto sendAttendanceCertificate(Integer userId, Integer stageId) {
        User user = findUser(userId);

        List<EventEnrollment> attended = eventEnrollmentRepository.findAttendedByUserId(userId).stream()
                .filter(ee -> stageId == null || stageId.equals(ee.getLessonBlock().getEducationStageId()))
                .toList();
        if (attended.isEmpty()) {
            throw new EcatlimException("La persona no tiene asistencias registradas", HttpStatus.BAD_REQUEST);
        }

        Map<Integer, Event> events = new LinkedHashMap<>();
        attended.forEach(ee -> events.putIfAbsent(ee.getEvent().getId(), ee.getEvent()));
        int blocks = (int) attended.stream().map(ee -> ee.getLessonBlock().getId()).distinct().count();

        String stageName = stageId == null ? null : attended.get(0).getLessonBlock().getModule().getEducationStage().getName();

        log.info("METHOD sendAttendanceCertificate() - User {} events {} blocks {}", userId, events.size(), blocks);
        emailService.sendAttendanceCertificateEmail(user.getEmail(), CertificatePdfService.fullName(user),
                events.size(), blocks, stageName, events.values().stream().map(Event::getTitle).toList());

        return new AttendanceSummaryDto(events.size(), blocks);
    }

    @Transactional
    public void setBlockPassed(Integer userId, Integer lessonBlockId, boolean passed) {
        UserLessonBlock progress = userLessonBlockRepository.findByUserIdAndLessonBlockId(userId, lessonBlockId)
                .orElseThrow(() -> new ResourceNotFoundException("La persona no está inscrita en la etapa de este bloque"));

        log.info("METHOD setBlockPassed() - User {} block {} passed {}", userId, lessonBlockId, passed);
        progress.setCompleted(passed);
        progress.setCompletionDate(passed ? new Date() : null);
        userLessonBlockRepository.save(progress);
    }

    @Transactional
    public void sendBlockCertificates(Integer userId, List<Integer> lessonBlockIds) {
        User user = findUser(userId);

        List<LessonBlock> blocks = lessonBlockIds.stream().distinct().map(blockId -> {
            UserLessonBlock progress = userLessonBlockRepository.findByUserIdAndLessonBlockId(userId, blockId)
                    .orElseThrow(() -> new ResourceNotFoundException("La persona no está inscrita en la etapa del bloque " + blockId));
            if (!progress.isCompleted()) {
                throw new EcatlimException("El bloque " + progress.getLessonBlock().getCode() + " todavía no está superado", HttpStatus.BAD_REQUEST);
            }
            return progress.getLessonBlock();
        }).sorted(Comparator.comparing(LessonBlock::getCode)).toList();

        log.info("METHOD sendBlockCertificates() - User {} blocks {}", userId, lessonBlockIds);
        emailService.sendBlockCertificateEmail(user.getEmail(), CertificatePdfService.fullName(user),
                blocks.stream().map(b -> b.getCode() + " · " + b.getName()).toList());
    }

    @Transactional
    public ResponseEntity<byte[]> downloadBlockCertificate(String userEmail, Integer lessonBlockId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        UserLessonBlock progress = userLessonBlockRepository.findByUserIdAndLessonBlockId(user.getId(), lessonBlockId)
                .orElseThrow(() -> new ResourceNotFoundException("Bloque no encontrado en tu ruta formativa"));
        if (!progress.isCompleted()) {
            throw new EcatlimException("Todavía no has superado este bloque formativo", HttpStatus.FORBIDDEN);
        }

        LessonBlock block = progress.getLessonBlock();
        byte[] pdf = pdfService.generateBlockCertificate(user, block, progress.getCompletionDate());
        return new FileTransferDto(pdf, "certificado-" + block.getCode() + ".pdf", PDF)
                .setContentDisposition(ContentDisposition.attachment())
                .asResponseEntity();
    }

    @Transactional
    public EnrollmentDocumentsDto uploadStageCertificate(Integer enrollmentId, MultipartFile file) {
        UserEducationStage enrollment = findEnrollment(enrollmentId);

        String lowerName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (file.isEmpty() || !lowerName.endsWith(".pdf")) {
            throw new EcatlimException("El certificado de etapa debe ser un PDF", HttpStatus.BAD_REQUEST);
        }

        UserFile previous = enrollment.getStageCertificate();
        enrollment.setStageCertificate(userFileService.storeFile(file, UserFileType.USER_EDUCATION_STAGE, "stage_certificate"));
        userStageRepository.save(enrollment);

        if (previous != null) {
            userFileService.deleteStoredFile(previous);
        }
        return EnrollmentDocumentsDto.fromEnrollment(enrollment);
    }

    @Transactional
    public void completeStage(Integer enrollmentId) {
        UserEducationStage enrollment = findEnrollment(enrollmentId);
        Integer userId = enrollment.getUser().getId();
        Integer stageId = enrollment.getEducationStage().getId();

        int total = lessonBlockRepository.countByStageId(stageId);
        int completed = userLessonBlockRepository.countCompletedByUserIdAndStageId(userId, stageId);
        if (total == 0 || completed < total) {
            throw new EcatlimException("Todavía hay bloques sin superar en esta etapa (" + completed + "/" + total + ")", HttpStatus.BAD_REQUEST);
        }

        log.info("METHOD completeStage() - Enrollment {} completed", enrollmentId);
        enrollment.setStatus(UserEducationStage.StageStatus.COMPLETED);
        enrollment.setCompleted(true);
        enrollment.setCompletionDate(new Date());
        userStageRepository.save(enrollment);
    }

    @Transactional
    public void sendStageCertificate(Integer enrollmentId) {
        UserEducationStage enrollment = findEnrollment(enrollmentId);
        if (enrollment.getStatus() != UserEducationStage.StageStatus.COMPLETED) {
            throw new EcatlimException("La etapa todavía no está completada", HttpStatus.BAD_REQUEST);
        }
        UserFile stageCertificate = enrollment.getStageCertificate();
        if (stageCertificate == null) {
            throw new EcatlimException("Sube primero el certificado de la etapa", HttpStatus.BAD_REQUEST);
        }

        User user = enrollment.getUser();
        String stageName = enrollment.getEducationStage().getName();
        byte[] diploma = pdfService.generateStageDiploma(user, stageName, enrollment.getCompletionDate());

        List<EmailAttachment> attachments = List.of(
                new EmailAttachment("diploma-" + enrollment.getEducationStage().getCode() + ".pdf", diploma, PDF),
                new EmailAttachment(stageCertificate.getName(), userFileService.readStoredFile(stageCertificate), PDF)
        );

        log.info("METHOD sendStageCertificate() - Enrollment {}", enrollmentId);
        emailService.sendStageCertificateEmail(user.getEmail(), CertificatePdfService.fullName(user), stageName, attachments);
        notificationService.notifyUsers(
                List.of(user.getId()),
                NotificationType.STAGE_CERTIFICATE_SENT,
                "Certificado de etapa disponible",
                "Has completado la etapa " + stageName + ". Te hemos enviado el diploma por correo.",
                "/app/mi-ruta-formacion",
                false,
                enrollmentId);
    }

    private User findUser(Integer userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private UserEducationStage findEnrollment(Integer enrollmentId) {
        return userStageRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción no encontrada"));
    }
}
