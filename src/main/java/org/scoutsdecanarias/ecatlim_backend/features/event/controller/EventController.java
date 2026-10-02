package org.scoutsdecanarias.ecatlim_backend.features.event.controller;

import org.scoutsdecanarias.ecatlim_backend.features.event.dto.*;
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

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGEMENT', 'EVENT_DIRECTOR')")
    @GetMapping
    public ResponseEntity<List<EventDto>> getAll() {
        return ResponseEntity.ok(eventService.findAll().stream().map(EventDto::fromEntity).toList());
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGEMENT', 'EVENT_DIRECTOR')")
    @GetMapping("/edit/{id}")
    public ResponseEntity<EventFormDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(EventFormDto.fromEntity(eventService.findById(id)));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGEMENT', 'EVENT_DIRECTOR')")
    @GetMapping("/user-calendar")
    public ResponseEntity<List<EventUserCalendarDto>> getUserCalendar() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(eventService.getEventsForUser(userEmail));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin/calendar")
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

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/admin/add")
    public EventDto create(@RequestBody EventFormDto event) {
        return EventDto.fromEntity(eventService.save(event));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/admin/{id}")
    public ResponseEntity<EventDto> update(@PathVariable Integer id, @RequestBody EventFormDto event) {
        return ResponseEntity.ok(EventDto.fromEntity(eventService.update(id, event)));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/admin/update-status/{id}")
    public ResponseEntity<EventDto> updateStatus(@PathVariable Integer id, @RequestBody String status) {
        return ResponseEntity.ok(EventDto.fromEntity(eventService.updateStatus(id, status)));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGEMENT', 'EVENT_DIRECTOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}