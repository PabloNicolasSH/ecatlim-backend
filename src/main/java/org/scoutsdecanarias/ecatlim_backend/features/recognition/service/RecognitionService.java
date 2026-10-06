package org.scoutsdecanarias.ecatlim_backend.features.recognition.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.LessonBlock;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.LessonBlockRepository;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.UserLessonBlock;
import org.scoutsdecanarias.ecatlim_backend.features.recognition.dto.RecognitionFormDto;
import org.scoutsdecanarias.ecatlim_backend.features.recognition.dto.RecognitionRequestDto;
import org.scoutsdecanarias.ecatlim_backend.features.recognition.dto.RecognitionResponseFormDto;
import org.scoutsdecanarias.ecatlim_backend.features.recognition.entity.RecognitionMessage;
import org.scoutsdecanarias.ecatlim_backend.features.recognition.entity.RecognitionRequest;
import org.scoutsdecanarias.ecatlim_backend.features.recognition.enums.RecognitionMessageKind;
import org.scoutsdecanarias.ecatlim_backend.features.recognition.enums.RecognitionStatus;
import org.scoutsdecanarias.ecatlim_backend.features.recognition.repository.RecognitionRequestRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.SimpleUserDto;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.enums.Role;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserLessonBlockRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user_file.UserFile;
import org.scoutsdecanarias.ecatlim_backend.features.user_file.UserFileService;
import org.scoutsdecanarias.ecatlim_backend.features.user_file.UserFileType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecognitionService {

    private static final int MAX_FILES_PER_MESSAGE = 10;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".pdf", ".doc", ".docx", ".jpg", ".jpeg", ".png");

    private static final List<Role> COMMISSION_ROLES = List.of(Role.TRAINER, Role.EVENT_DIRECTOR, Role.MANAGEMENT, Role.MANAGER_DIRECTOR);

    private final RecognitionRequestRepository recognitionRepository;
    private final UserRepository userRepository;
    private final LessonBlockRepository lessonBlockRepository;
    private final UserLessonBlockRepository userLessonBlockRepository;
    private final UserFileService userFileService;

    public List<RecognitionRequestDto> getMyRequests(String email) {
        User user = findUser(email);
        return RecognitionRequestDto.fromCollection(recognitionRepository.findByUserIdOrderByCreatedAtDesc(user.getId()));
    }

    /** Every staff member can read every request, resolved ones included, so past decisions can be used as precedent. */
    public List<RecognitionRequestDto> getRequestsForReview(Set<RecognitionStatus> statuses) {
        Set<RecognitionStatus> filter = statuses == null || statuses.isEmpty() ? EnumSet.allOf(RecognitionStatus.class) : statuses;
        return RecognitionRequestDto.fromCollectionForStaff(recognitionRepository.findByStatusInOrderByUpdatedAtDesc(filter));
    }

    public RecognitionRequestDto getRequest(Integer id) {
        return RecognitionRequestDto.fromEntityForStaff(findRequest(id));
    }

    /** Requests waiting for this person: unassigned ones for managers, plus those awaiting a reply from their commission. */
    public int countPendingForStaff(String email) {
        User staff = findUser(email);
        boolean manager = isManager(staff);
        return (int) recognitionRepository.findByStatusInOrderByUpdatedAtDesc(EnumSet.of(RecognitionStatus.PENDING_REVIEW)).stream()
                .filter(request -> isInCommission(request, staff) || (manager && request.getCommission().isEmpty()))
                .count();
    }

    public List<SimpleUserDto> getCommissionCandidates() {
        return SimpleUserDto.fromCollection(COMMISSION_ROLES.stream()
                .flatMap(role -> userRepository.findAllByRolesContaining(role).stream())
                .filter(User::isEnabled)
                .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new))
                .values().stream().toList());
    }

    @Transactional
    public RecognitionRequestDto assignCommission(Integer requestId, List<Integer> userIds) {
        RecognitionRequest request = findRequest(requestId);
        if (!request.getStatus().isActive()) {
            throw new EcatlimException("Esta solicitud ya está resuelta", HttpStatus.CONFLICT);
        }

        Set<Integer> distinctIds = new LinkedHashSet<>(userIds);
        List<User> members = userRepository.findAllById(distinctIds);
        if (members.size() != distinctIds.size()) {
            throw new EcatlimException("Alguna de las personas elegidas no existe", HttpStatus.BAD_REQUEST);
        }
        boolean invalid = members.stream().anyMatch(u -> !u.isEnabled() || u.getRoles().stream().noneMatch(COMMISSION_ROLES::contains));
        if (invalid) {
            throw new EcatlimException("La comisión solo puede estar formada por personas del equipo formativo", HttpStatus.BAD_REQUEST);
        }

        request.getCommission().clear();
        request.getCommission().addAll(members);
        request.setUpdatedAt(ZonedDateTime.now());

        log.info("METHOD assignCommission() - Recognition request {} commission set to {}", requestId, distinctIds);
        return RecognitionRequestDto.fromEntityForStaff(recognitionRepository.save(request));
    }

    @Transactional
    public RecognitionRequestDto openRequest(String email, Integer lessonBlockId, RecognitionFormDto form, List<MultipartFile> files) {
        User user = findUser(email);
        LessonBlock block = lessonBlockRepository.findById(lessonBlockId)
                .orElseThrow(() -> new EntityNotFoundException("Bloque formativo no encontrado"));

        if (!block.isRecognizable()) {
            throw new EcatlimException("Este bloque formativo no es convalidable", HttpStatus.BAD_REQUEST);
        }
        UserLessonBlock progress = userLessonBlockRepository.findByUserIdAndLessonBlockId(user.getId(), lessonBlockId)
                .orElseThrow(() -> new EcatlimException("Debes estar inscrito en la etapa de este bloque para convalidarlo", HttpStatus.BAD_REQUEST));
        if (progress.isCompleted()) {
            throw new EcatlimException("Ya has superado este bloque formativo", HttpStatus.BAD_REQUEST);
        }
        if (recognitionRepository.existsByUserIdAndLessonBlockIdAndStatusIn(user.getId(), lessonBlockId,
                EnumSet.of(RecognitionStatus.PENDING_REVIEW, RecognitionStatus.AWAITING_DOCUMENTATION))) {
            throw new EcatlimException("Ya tienes una solicitud de convalidación abierta para este bloque", HttpStatus.CONFLICT);
        }

        ZonedDateTime now = ZonedDateTime.now();
        RecognitionRequest request = new RecognitionRequest();
        request.setUser(user);
        request.setLessonBlock(block);
        request.setType(form.type());
        request.setStatus(RecognitionStatus.PENDING_REVIEW);
        request.setCreatedAt(now);
        request.setUpdatedAt(now);
        request.addMessage(buildMessage(user, RecognitionMessageKind.SUBMISSION, form.comment(), files, now));

        log.info("METHOD openRequest() - User {} requests recognition of block {} by {}", user.getId(), lessonBlockId, form.type());
        return RecognitionRequestDto.fromEntity(recognitionRepository.save(request));
    }

    @Transactional
    public RecognitionRequestDto addDocumentation(String email, Integer requestId, String comment, List<MultipartFile> files) {
        User user = findUser(email);
        RecognitionRequest request = findRequest(requestId);

        if (!request.getUser().getId().equals(user.getId())) {
            throw new EcatlimException("No puedes modificar esta solicitud", HttpStatus.FORBIDDEN);
        }
        if (request.getStatus() != RecognitionStatus.AWAITING_DOCUMENTATION) {
            throw new EcatlimException("Esta solicitud no está esperando documentación", HttpStatus.CONFLICT);
        }
        if (comment != null && comment.length() > 2000) {
            throw new EcatlimException("El comentario no puede superar los 2000 caracteres", HttpStatus.BAD_REQUEST);
        }
        if (isBlank(comment) && nonEmptyFiles(files).isEmpty()) {
            throw new EcatlimException("Adjunta algún archivo o escribe un comentario", HttpStatus.BAD_REQUEST);
        }

        request.addMessage(buildMessage(user, RecognitionMessageKind.SUBMISSION, comment, files, ZonedDateTime.now()));
        request.setStatus(RecognitionStatus.PENDING_REVIEW);

        log.info("METHOD addDocumentation() - User {} adds documentation to recognition request {}", user.getId(), requestId);
        return RecognitionRequestDto.fromEntity(recognitionRepository.save(request));
    }

    @Transactional
    public RecognitionRequestDto respond(String teamEmail, Integer requestId, RecognitionResponseFormDto form) {
        User responder = findUser(teamEmail);
        RecognitionRequest request = findRequest(requestId);

        if (request.getCommission().isEmpty()) {
            throw new EcatlimException("Todavía no se ha asignado una comisión a esta solicitud", HttpStatus.CONFLICT);
        }
        if (!isInCommission(request, responder)) {
            throw new EcatlimException("No formas parte de la comisión de esta solicitud", HttpStatus.FORBIDDEN);
        }
        if (!request.getStatus().isActive()) {
            throw new EcatlimException("Esta solicitud ya está resuelta", HttpStatus.CONFLICT);
        }
        if (request.getStatus() != RecognitionStatus.PENDING_REVIEW) {
            throw new EcatlimException("Estás esperando la documentación que ya se solicitó", HttpStatus.CONFLICT);
        }

        ZonedDateTime now = ZonedDateTime.now();
        switch (form.decision()) {
            case APPROVE -> {
                markBlockPassed(request);
                request.setStatus(RecognitionStatus.APPROVED);
                request.addMessage(buildMessage(responder, RecognitionMessageKind.APPROVED, form.comment(), null, now));
            }
            case REJECT -> {
                request.setStatus(RecognitionStatus.REJECTED);
                request.addMessage(buildMessage(responder, RecognitionMessageKind.REJECTED, form.comment(), null, now));
            }
            case REQUEST_DOCUMENTATION -> {
                if (isBlank(form.comment())) {
                    throw new EcatlimException("Indica qué documentación necesitas", HttpStatus.BAD_REQUEST);
                }
                request.setStatus(RecognitionStatus.AWAITING_DOCUMENTATION);
                request.addMessage(buildMessage(responder, RecognitionMessageKind.DOCUMENTATION_REQUESTED, form.comment(), null, now));
            }
        }

        log.info("METHOD respond() - {} answers {} to recognition request {}", responder.getId(), form.decision(), requestId);
        return RecognitionRequestDto.fromEntityForStaff(recognitionRepository.save(request));
    }

    private boolean isManager(User user) {
        return user.getRoles().contains(Role.MANAGEMENT) || user.getRoles().contains(Role.MANAGER_DIRECTOR);
    }

    private boolean isInCommission(RecognitionRequest request, User user) {
        return request.getCommission().stream().anyMatch(member -> member.getId().equals(user.getId()));
    }

    private void markBlockPassed(RecognitionRequest request) {
        UserLessonBlock progress = userLessonBlockRepository
                .findByUserIdAndLessonBlockId(request.getUser().getId(), request.getLessonBlock().getId())
                .orElseThrow(() -> new EcatlimException("La persona no está inscrita en la etapa de este bloque", HttpStatus.CONFLICT));
        progress.setCompleted(true);
        progress.setCompletionDate(new Date());
        userLessonBlockRepository.save(progress);
    }

    private RecognitionMessage buildMessage(User author, RecognitionMessageKind kind, String comment,
                                            List<MultipartFile> files, ZonedDateTime now) {
        RecognitionMessage message = new RecognitionMessage();
        message.setAuthor(author);
        message.setKind(kind);
        message.setComment(isBlank(comment) ? null : comment.trim());
        message.setCreatedAt(now);

        List<MultipartFile> valid = nonEmptyFiles(files);
        if (valid.size() > MAX_FILES_PER_MESSAGE) {
            throw new EcatlimException("Puedes adjuntar como máximo " + MAX_FILES_PER_MESSAGE + " archivos cada vez", HttpStatus.BAD_REQUEST);
        }
        valid.forEach(this::validateFile);
        valid.forEach(file -> {
            UserFile stored = userFileService.storeFile(file, UserFileType.RECOGNITION, "recognition");
            message.getFiles().add(stored);
        });
        return message;
    }

    private void validateFile(MultipartFile file) {
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        int dot = name.lastIndexOf('.');
        if (dot < 0 || !ALLOWED_EXTENSIONS.contains(name.substring(dot))) {
            throw new EcatlimException("Los archivos deben ser PDF, Word o imágenes (JPG, PNG)", HttpStatus.BAD_REQUEST);
        }
    }

    private List<MultipartFile> nonEmptyFiles(List<MultipartFile> files) {
        return files == null ? List.of() : files.stream().filter(f -> f != null && !f.isEmpty()).toList();
    }

    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
    }

    private RecognitionRequest findRequest(Integer id) {
        return recognitionRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Solicitud de convalidación no encontrada"));
    }
}
