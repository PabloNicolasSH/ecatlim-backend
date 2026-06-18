package org.scoutsdecanarias.ecatlim_backend.features.event.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.scoutsdecanarias.ecatlim_backend.features.event.dto.*;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.LessonBlockRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.SimpleUserDto;
import org.scoutsdecanarias.ecatlim_backend.features.user.Role;
import org.scoutsdecanarias.ecatlim_backend.event.dto.*;
import org.scoutsdecanarias.ecatlim_backend.dto.TimelineItemFormDto;
import org.scoutsdecanarias.ecatlim_backend.features.event.entity.Event;
import org.scoutsdecanarias.ecatlim_backend.entity.LessonBlock;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.event.entity.EventConfiguration;
import org.scoutsdecanarias.ecatlim_backend.features.event.enums.EventStatus;
import org.scoutsdecanarias.ecatlim_backend.features.event.enums.NotificationTarget;
import org.scoutsdecanarias.ecatlim_backend.features.event.repository.EventRepository;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.dto.LessonBlockCalendarSummaryDto;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserLessonBlockRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.scoutsdecanarias.ecatlim_backend.repository.*;
import org.scoutsdecanarias.ecatlim_backend.shared.email.EmailService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final LessonBlockRepository lessonBlockRepository;
    private final EducationStageRepository educationStageRepository;
    private final UserLessonBlockRepository userLessonBlockRepository;
    private final EventConfigurationService eventConfigurationService;
    private final EmailService emailService;

    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    public Event findById(Integer id) {
        return eventRepository.findById(id).orElseThrow(NoSuchElementException::new);
    }

    public List<EventUserCalendarDto> getEventsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        List<Event> allEvents = eventRepository.findAllByStatus(EventStatus.PUBLISHED);
        return allEvents.stream()
                .map(event ->  new EventUserCalendarDto(
                                event.getId(),
                                event.getTitle(),
                                event.getShortname(),
                                event.getDescription(),
                                event.getContents(),
                                event.getStartDate(),
                                event.getEndDate(),
                                event.getLocation(),
                                event.getOrganizer(),
                                event.getLessonBlocks().stream().map(LessonBlockCalendarSummaryDto::fromEntity).toList(),
                                event.getAttendees().size(),
                                event.getEducationStageCode(),
                                event.getAttendees().contains(user),
                                this.calculateParticipation(event, user),
                                event.isClosed(),
                                event.getStatus().toString()
                        )
                )
                .toList();
    }

    public List<EventAdminCalendarDto> getEventsForAdmin(String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        List<Event> allEvents = eventRepository.findAll();
        return allEvents.stream()
                .map(event ->  new EventAdminCalendarDto(
                        event.getId(),
                        event.getTitle(),
                        event.getShortname(),
                        event.getDescription(),
                        event.getContents(),
                        event.getStartDate(),
                        event.getEndDate(),
                        event.getLocation(),
                        event.getOrganizer(),
                        event.getLessonBlocks().stream().map(LessonBlockCalendarSummaryDto::fromEntity).toList(),
                        SimpleUserDto.fromEntity(event.getDirector()),
                        EnrolledUserDto.fromCollection(event.getAttendees()),
                        event.getStatus().toString(),
                        EventConfigDto.fromEntity(event.getEventConfiguration())
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

        Set<NotificationTarget> notificationTargets = new HashSet<>();

        if (form.eventConfiguration().notificationTarget() != null) {
            for (String target : form.eventConfiguration().notificationTarget()) {
                notificationTargets.add(NotificationTarget.valueOf(target));
            }
        } else {
            notificationTargets.add(NotificationTarget.INTERESTED_USERS);
        }

        eventRepository.save(event);

        if (EventStatus.valueOf(form.status()).equals(EventStatus.PUBLISHED)){
            this.sendEmailToNotifyEvent(event, notificationTargets);
        }

        return event;
    }

    public Event update(Integer id, EventFormDto form) {
        Event existingEvent = eventRepository.findByIdWithTimeline(id)
                .orElseThrow(() -> new EntityNotFoundException("Evento no encontrado"));

        updateEventFromDto(existingEvent, form);

        return eventRepository.save(existingEvent);
    }

    public Event updateStatus(Integer id, String status) {
        Event existingEvent = eventRepository.findByIdWithTimeline(id)
                .orElseThrow(() -> new EntityNotFoundException("Evento no encontrado"));

        existingEvent.setStatus(EventStatus.valueOf(status));
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

    public Event unenrollStudent(Integer id, String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        Event event = eventRepository.findById(id).orElseThrow();

        event.getAttendees().remove(user);
        return eventRepository.save(event);
    }

    private void updateEventFromDto(Event event, EventFormDto form) {
        event.setTitle(form.title());
        event.setDescription(form.description());
        event.setContents(form.contents());
        event.setShortname(form.shortname());
        event.setStartDate(form.startDate());
        event.setEndDate(form.endDate());
        event.setLocation(form.location());
        event.setOrganizer(form.organizer());
        event.setStatus(EventStatus.valueOf(form.status()));

        event.setDirector(userRepository.findById(form.directorId())
                .orElseThrow(() -> new EntityNotFoundException("Director no encontrado")));

        event.setEducationStage(educationStageRepository.findEducationStageById(form.educationStageId()));
        List<LessonBlock> blocks = lessonBlockRepository.findAllById(form.lessonBlockIds());
        if (blocks.size() != form.lessonBlockIds().size()) {
            throw new EntityNotFoundException("Algunos bloques de aprendizaje no existen");
        }

        event.setLessonBlocks(new HashSet<>(blocks));
        event.setTheoreticalHours(blocks.stream().mapToInt(LessonBlock::getContactHours).sum());
        event.setOnlineHours(blocks.stream().mapToInt(LessonBlock::getOnlineHours).sum());

        List<User> facilitator = userRepository.findAllById(form.facilitatorIds());
        if (facilitator.size() != form.facilitatorIds().size()) {
            throw new EntityNotFoundException("Algunos formadores no existen");
        }

        event.setFacilitators(new HashSet<>(facilitator));

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

        if (form.eventConfiguration() != null) {
            EventConfigDto eventConfiguration = form.eventConfiguration();
            EventConfiguration eventConfigAdded = eventConfigurationService.add(eventConfiguration);
            event.setEventConfiguration(eventConfigAdded);
        }
    }

    private void sendEmailToNotifyEvent(Event event, Set<NotificationTarget> notificationTargets) {
        if (notificationTargets == null || notificationTargets.isEmpty()) return;

        Set<LessonBlock> eventLessonBlocks = event.getLessonBlocks();

        Set<User> recipients = new HashSet<>();

        if (notificationTargets.contains(NotificationTarget.HEAD_OF_EDUCATION)) {
            List<User> headsOfEducation = userRepository.findAllByRole(Role.HEAD_OF_EDUCATION);
            recipients.addAll(headsOfEducation);
        }

        if (notificationTargets.contains(NotificationTarget.AVAILABLE_USERS)) {
            List<User> activeUsers = userRepository.findAllByEnabled(true);
            recipients.addAll(activeUsers);
        }

        List<User> interestedUsers = new ArrayList<>();
        if (notificationTargets.contains(NotificationTarget.INTERESTED_USERS) && !eventLessonBlocks.isEmpty()) {
            interestedUsers = userLessonBlockRepository.findActiveAndEligibleUsers(eventLessonBlocks);
            recipients.addAll(interestedUsers);
        }

        if (recipients.isEmpty()) return;

        List<String> allEventBlockTitles = eventLessonBlocks.stream()
                .map(LessonBlock::getName)
                .toList();

        for (User user : recipients) {
            List<String> missingBlockTitles = new ArrayList<>();

            if (interestedUsers.contains(user) && !eventLessonBlocks.isEmpty()) {
                missingBlockTitles = eventLessonBlocks.stream()
                        .filter(block -> !userLessonBlockRepository.existsByUserAndLessonBlockAndCompletedTrue(user, block))
                        .map(LessonBlock::getName)
                        .toList();
            }

            emailService.sendEventRecommendationEmail(
                    user.getEmail(),
                    user.getName(),
                    event.getTitle(),
                    event.getLocation(),
                    this.formatDate(event.getStartDate()),
                    missingBlockTitles,
                    allEventBlockTitles
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
