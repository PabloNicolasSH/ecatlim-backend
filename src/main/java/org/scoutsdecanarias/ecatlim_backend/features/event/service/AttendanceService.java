package org.scoutsdecanarias.ecatlim_backend.features.event.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.scoutsdecanarias.ecatlim_backend.core.exception.ResourceNotFoundException;
import org.scoutsdecanarias.ecatlim_backend.features.event.dto.AttendanceFormDto;
import org.scoutsdecanarias.ecatlim_backend.features.event.entity.Event;
import org.scoutsdecanarias.ecatlim_backend.features.event.entity.EventEnrollment;
import org.scoutsdecanarias.ecatlim_backend.features.event.repository.EventEnrollmentRepository;
import org.scoutsdecanarias.ecatlim_backend.features.event.repository.EventRepository;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.LessonBlock;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.enums.Role;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventEnrollmentRepository eventEnrollmentRepository;

    public Set<Integer> getMarkableLessonBlockIds(Event event, String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return Set.of();
        }

        boolean isDirector = event.getDirector() != null && event.getDirector().getId().equals(user.getId());
        if (isDirector || user.getRoles().contains(Role.ADMIN)) {
            return event.getLessonBlocks().stream().map(LessonBlock::getId).collect(Collectors.toSet());
        }

        Set<Integer> blockIds = new HashSet<>();
        event.getTimelineItems().stream()
                .filter(item -> item.isFormative() && item.getEducationSession() != null)
                .map(item -> item.getEducationSession())
                .filter(session -> session.getLessonBlock() != null && session.getFacilitators() != null
                        && session.getFacilitators().stream().anyMatch(f -> f.getId().equals(user.getId())))
                .forEach(session -> blockIds.add(session.getLessonBlock().getId()));
        return blockIds;
    }

    @Transactional
    public void markAttendance(Integer eventId, AttendanceFormDto form, String requesterEmail) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado"));

        if (!getMarkableLessonBlockIds(event, requesterEmail).contains(form.lessonBlockId())) {
            throw new EcatlimException("No puedes registrar la asistencia de este bloque formativo", HttpStatus.FORBIDDEN);
        }

        if (event.getStartDate().isAfter(LocalDateTime.now())) {
            throw new EcatlimException("El evento todavía no ha comenzado", HttpStatus.BAD_REQUEST);
        }

        EventEnrollment enrollment = eventEnrollmentRepository
                .findByUserIdAndEventIdAndLessonBlockId(form.userId(), eventId, form.lessonBlockId())
                .orElseThrow(() -> new ResourceNotFoundException("La persona no está inscrita en este bloque del evento"));

        log.info("METHOD markAttendance() - Event {} user {} block {} attendance {}",
                eventId, form.userId(), form.lessonBlockId(), form.attendance());
        enrollment.setAttendance(form.attendance());
        eventEnrollmentRepository.save(enrollment);
    }
}
