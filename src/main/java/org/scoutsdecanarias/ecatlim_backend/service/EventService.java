package org.scoutsdecanarias.ecatlim_backend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.scoutsdecanarias.ecatlim_backend.dto.event.EventFormDto;
import org.scoutsdecanarias.ecatlim_backend.dto.event.EventHomeWidgetDto;
import org.scoutsdecanarias.ecatlim_backend.dto.event.EventUserCalendarDto;
import org.scoutsdecanarias.ecatlim_backend.dto.TimelineItemFormDto;
import org.scoutsdecanarias.ecatlim_backend.entity.EducationSession;
import org.scoutsdecanarias.ecatlim_backend.entity.Event;
import org.scoutsdecanarias.ecatlim_backend.entity.LessonBlock;
import org.scoutsdecanarias.ecatlim_backend.entity.TimelineItem;
import org.scoutsdecanarias.ecatlim_backend.entity.User;
import org.scoutsdecanarias.ecatlim_backend.repository.EventRepository;
import org.scoutsdecanarias.ecatlim_backend.repository.LessonBlockRepository;
import org.scoutsdecanarias.ecatlim_backend.repository.UserEducationStageRepository;
import org.scoutsdecanarias.ecatlim_backend.repository.UserLessonBlockRepository;
import org.scoutsdecanarias.ecatlim_backend.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final LessonBlockRepository lessonBlockRepository;
    private final UserEducationStageRepository userEducationStageRepository;
    private final UserLessonBlockRepository userLessonBlockRepository;
    private final EmailService emailService;

    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    public Event findById(Integer id) {
        return eventRepository.findById(id).orElseThrow(NoSuchElementException::new);
    }

    public List<EventUserCalendarDto> getEventsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        List<Event> allEvents = eventRepository.findAll();
        return allEvents.stream()
                .map(event ->  new EventUserCalendarDto(
                            event.getId(),
                            event.getTitle(),
                            event.getDescription(),
                            event.getStartDate(),
                            event.getEndDate(),
                            event.getLocation(),
                            event.getOrganizer(),
                            event.getLessonBlocks().stream().map(LessonBlock::getCode).toList(),
                            event.getAttendees().size(),
                            event.getEducationStageCode(),
                            event.getAttendees().contains(user),
                            this.calculateParticipation(event, user)
                    )
                )
                .toList();
    }

    public List<EventHomeWidgetDto> getUpcomingEventsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Event> upcomingEvents = eventRepository.findUpcomingEvents(
                LocalDateTime.now(),
                PageRequest.of(0, 5)
        );

        return upcomingEvents.stream()
                .filter(e -> this.calculateParticipation(e, user) || e.getAttendees().contains(user))
                .map(event -> new EventHomeWidgetDto(
                        event.getId(),
                        event.getTitle(),
                        event.getStartDate(),
                        event.getLocation(),
                        event.getEducationStageCode(),
                        event.getAttendees().contains(user),
                        this.calculateParticipation(event, user)
                ))
                .toList();
    }

    public Event save(EventFormDto form) {
        Event event = new Event();
        updateEventFromDto(event, form);

        eventRepository.save(event);
        this.sendEmailToPossibleAttendees(event);

        return event;
    }

    public Event update(Integer id, EventFormDto form) {
        Event existingEvent = eventRepository.findByIdWithTimeline(id)
                .orElseThrow(() -> new EntityNotFoundException("Evento no encontrado"));

        updateEventFromDto(existingEvent, form);

        return eventRepository.save(existingEvent);
    }

    public void delete(Integer id) {
        eventRepository.deleteById(id);
    }

    public Event enrollStudent(Integer id, String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        Event event = eventRepository.findById(id).orElseThrow();

        event.getAttendees().add(user);
        return eventRepository.save(event);
    }

    private void updateEventFromDto(Event event, EventFormDto form) {
        event.setTitle(form.title());
        event.setDescription(form.description());
        event.setStartDate(form.startDate());
        event.setEndDate(form.endDate());
        event.setLocation(form.location());
        event.setOrganizer(form.organizer());

        event.setDirector(userRepository.findById(form.directorId())
                .orElseThrow(() -> new EntityNotFoundException("Director no encontrado")));

        List<LessonBlock> blocks = lessonBlockRepository.findAllById(form.lessonBlockIds());
        if (blocks.size() != form.lessonBlockIds().size()) {
            throw new EntityNotFoundException("Algunos bloques de aprendizaje no existen");
        }

        event.setLessonBlocks(new HashSet<>(blocks));
        event.setTheoreticalHours(blocks.stream().mapToInt(LessonBlock::getContactHours).sum());
        event.setOnlineHours(blocks.stream().mapToInt(LessonBlock::getOnlineHours).sum());

        if (event.getTimelineItems() != null) {
            event.getTimelineItems().clear();
        }

        if (form.timelineItems() != null) {
            TimelineItemFormDto.fromDtoCollection(form.timelineItems()).forEach(item -> {
                event.addTimelineItem(item);
                if (item.isFormative() && item.getEducationSession() != null) {
                    item.getEducationSession().setTimelineItem(item);
                }
            });
        }
    }

    private void sendEmailToPossibleAttendees(Event event) {
        Set<LessonBlock> eventLessonBlocks = event.getLessonBlocks();

        if (eventLessonBlocks.isEmpty()) return;

        List<User> possibleAttendees = userLessonBlockRepository.findActiveAndEligibleUsers(eventLessonBlocks);

        for (User user : possibleAttendees) {
            List<String> missingBlockTitles = eventLessonBlocks.stream()
                    .filter(block -> !userLessonBlockRepository.existsByUserAndLessonBlockAndCompletedTrue(user, block))
                    .map(LessonBlock::getName)
                    .toList();

            emailService.sendEventRecommendationEmail(
                    user.getEmail(),
                    user.getName(),
                    event.getTitle(),
                    event.getLocation(),
                    this.formatDate(event.getStartDate()),
                    missingBlockTitles
            );
        }
    }

    private boolean calculateParticipation(Event event, User user) {
        Set<LessonBlock> eventLessonBlocks = event.getLessonBlocks();
        List<User> possibleAttendees = userLessonBlockRepository.findActiveAndEligibleUsers(eventLessonBlocks);
        return possibleAttendees.contains(user);
    }

    private String formatDate(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return date.format(formatter);
    }
}
