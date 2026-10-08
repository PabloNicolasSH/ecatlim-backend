package org.scoutsdecanarias.ecatlim_backend.features.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.scoutsdecanarias.ecatlim_backend.features.event.dto.*;
import org.scoutsdecanarias.ecatlim_backend.features.event.service.AttendanceService;
import org.scoutsdecanarias.ecatlim_backend.features.event.service.EventService;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.dto.LessonBlockDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    private final AttendanceService attendanceService;

    public EventController(EventService eventService, AttendanceService attendanceService) {
        this.eventService = eventService;
        this.attendanceService = attendanceService;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGEMENT', 'EVENT_DIRECTOR')")
    @GetMapping
    public ResponseEntity<List<EventDto>> getAll() {
        return ResponseEntity.ok(eventService.findAll().stream().map(EventDto::fromEntity).toList());
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER_DIRECTOR', 'MANAGEMENT', 'EVENT_DIRECTOR')")
    @GetMapping("/suggestions")
    public ResponseEntity<EventSuggestionsDto> getSuggestions() {
        return ResponseEntity.ok(eventService.getSuggestions());
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER_DIRECTOR', 'MANAGEMENT', 'EVENT_DIRECTOR', 'TRAINER')")
    @GetMapping("/{id}/detail")
    public ResponseEntity<EventDetailDto> getDetail(@PathVariable Integer id) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(eventService.getEventDetail(id, userEmail));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER_DIRECTOR', 'MANAGEMENT', 'EVENT_DIRECTOR') or @eventSecurity.isDirector(#id, authentication.name)")
    @GetMapping("/edit/{id}")
    public ResponseEntity<EventFormDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(EventFormDto.fromEntity(eventService.findById(id)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user-calendar")
    public ResponseEntity<List<EventUserCalendarDto>> getUserCalendar() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(eventService.getEventsForUser(userEmail));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER_DIRECTOR', 'MANAGEMENT', 'EVENT_DIRECTOR', 'TRAINER')")
    @GetMapping("/calendar")
    public ResponseEntity<List<EventAdminCalendarDto>> getAdminCalendar() {
        return ResponseEntity.ok(eventService.getEventsForAdmin());
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGEMENT', 'EVENT_DIRECTOR')")
    @GetMapping("/user-home")
    public ResponseEntity<List<EventHomeWidgetDto>> getUserHome() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(eventService.getUpcomingEventsForUser(userEmail));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGEMENT', 'EVENT_DIRECTOR')")
    @GetMapping("/{id}/lesson-blocks")
    public ResponseEntity<List<LessonBlockDto>> getLessonBlocks(@PathVariable Integer id) {
        return ResponseEntity.ok(LessonBlockDto.fromCollections(eventService.getEventLessonBlocks(id)));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER_DIRECTOR', 'MANAGEMENT', 'EVENT_DIRECTOR')")
    @PostMapping("/add")
    public EventDto create(@Valid @RequestBody EventFormDto event) {
        return EventDto.fromEntity(eventService.save(event));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER_DIRECTOR', 'MANAGEMENT', 'EVENT_DIRECTOR') or @eventSecurity.isDirector(#id, authentication.name)")
    @PutMapping("/{id}")
    public ResponseEntity<EventDto> update(@PathVariable Integer id, @Valid @RequestBody EventFormDto event) {
        return ResponseEntity.ok(EventDto.fromEntity(eventService.update(id, event)));
    }

    @PreAuthorize("hasAuthority('MANAGER_DIRECTOR')")
    @PutMapping("/update-status/{id}")
    public ResponseEntity<EventDto> updateStatus(@PathVariable Integer id, @RequestBody @NotBlank(message = "El estado es obligatorio") @Pattern(regexp = "PUBLISHED|PENDING|DRAFT", message = "El estado del evento no es válido") String status) {
        return ResponseEntity.ok(EventDto.fromEntity(eventService.updateStatus(id, status)));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER_DIRECTOR', 'MANAGEMENT', 'EVENT_DIRECTOR') or @eventSecurity.isDirector(#id, authentication.name)")
    @PutMapping("/{id}/set-pending")
    public ResponseEntity<EventDto> setPending(@PathVariable Integer id) {
        return ResponseEntity.ok(EventDto.fromEntity(eventService.setPending(id)));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGEMENT', 'EVENT_DIRECTOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVENT_DIRECTOR', 'TRAINER')")
    @PutMapping("/{id}/attendance")
    public ResponseEntity<Void> markAttendance(@PathVariable Integer id, @Valid @RequestBody AttendanceFormDto form) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        attendanceService.markAttendance(id, form, userEmail);
        return ResponseEntity.noContent().build();
    }
}
