package org.scoutsdecanarias.ecatlim_backend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.scoutsdecanarias.ecatlim_backend.dto.EventFormDto;
import org.scoutsdecanarias.ecatlim_backend.dto.TimelineItemDto;
import org.scoutsdecanarias.ecatlim_backend.entity.EducationSession;
import org.scoutsdecanarias.ecatlim_backend.entity.EducationStage;
import org.scoutsdecanarias.ecatlim_backend.entity.Event;
import org.scoutsdecanarias.ecatlim_backend.entity.LessonBlock;
import org.scoutsdecanarias.ecatlim_backend.entity.TimelineItem;
import org.scoutsdecanarias.ecatlim_backend.entity.User;
import org.scoutsdecanarias.ecatlim_backend.repository.EventRepository;
import org.scoutsdecanarias.ecatlim_backend.repository.LessonBlockRepository;
import org.scoutsdecanarias.ecatlim_backend.repository.UserEducationStageRepository;
import org.scoutsdecanarias.ecatlim_backend.repository.UserLessonBlockRepository;
import org.scoutsdecanarias.ecatlim_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

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

    public Event save(EventFormDto form) {
        Event event = new Event();
        event.setTitle(form.title());
        event.setDescription(form.description());
        event.setStartDate(form.startDate());
        event.setEndDate(form.endDate());
        event.setLocation(form.location());
        event.setOrganizer(form.organizer());
        event.setDirector(userRepository.findById(form.directorId()).orElseThrow());
        if (form.timeline() != null) {
            event.setTimelineItems(TimelineItemDto.fromDtoCollection(form.timeline()));
        }

        List<LessonBlock> blocks = lessonBlockRepository.findAllById(form.lessonBlockIds());
        if (blocks.isEmpty()) throw new EntityNotFoundException("No se encontraron bloques de aprendizaje");
        if (blocks.size() != form.lessonBlockIds().size()) throw new EntityNotFoundException("Algunos bloques de aprendizaje no existen");
        event.setLessonBlocks(new HashSet<>(blocks));

        int totalTheoreticalHours = blocks.stream()
                .mapToInt(LessonBlock::getContactHours)
                .sum();

        int totalOnlineHours = blocks.stream()
                .mapToInt(LessonBlock::getOnlineHours)
                .sum();

        event.setOnlineHours(totalOnlineHours);
        event.setTheoreticalHours(totalTheoreticalHours);

        if (event.getTimelineItems() != null) {
            event.getTimelineItems().forEach(item -> {
                item.setEvent(event);

                if (item.getEducationSession() != null) {
                    EducationSession session = item.getEducationSession();
                    session.setTimelineItem(item);

                    //TODO: ECL-11 add activities and resources
                }
            });
        }
        eventRepository.save(event);

        this.sendEmailToPossibleAttendees(event);

        return event;
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

    public Event update(Integer id, Event eventDetails) {
        Event existingEvent = eventRepository.findByIdWithTimeline(id)
                .orElseThrow(() -> new EntityNotFoundException("Evento no encontrado"));

        existingEvent.setTitle(eventDetails.getTitle());
        existingEvent.setStartDate(eventDetails.getStartDate());
        existingEvent.setEndDate(eventDetails.getEndDate());
        existingEvent.setLocation(eventDetails.getLocation());
        existingEvent.setOrganizer(eventDetails.getOrganizer());

        existingEvent.setLessonBlocks(eventDetails.getLessonBlocks());

        existingEvent.getTimelineItems().clear();

        if (eventDetails.getTimelineItems() != null) {
            for (TimelineItem newItem : eventDetails.getTimelineItems()) {
                existingEvent.addTimelineItem(newItem);

                if (newItem.getEducationSession() != null) {
                    EducationSession session = newItem.getEducationSession();
                    session.setTimelineItem(newItem);
                    //TODO: ECL-11 add activities and resources
                }
            }
        }

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

    private String formatDate(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return date.format(formatter);
    }
}
