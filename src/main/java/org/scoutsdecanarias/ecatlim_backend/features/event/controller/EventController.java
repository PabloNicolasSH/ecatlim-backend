package org.scoutsdecanarias.ecatlim_backend.features.event.controller;

import org.scoutsdecanarias.ecatlim_backend.features.event.dto.*;
import org.scoutsdecanarias.ecatlim_backend.features.event.service.EventService;
import org.springframework.http.ResponseEntity;
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

    @GetMapping
    public ResponseEntity<List<EventDto>> getAll() {
        return ResponseEntity.ok(eventService.findAll().stream().map(EventDto::fromEntity).toList());
    }

    @GetMapping("/edit/{id}")
    public ResponseEntity<EventFormDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(EventFormDto.fromEntity(eventService.findById(id)));
    }

    @GetMapping("/user-calendar")
    public ResponseEntity<List<EventUserCalendarDto>> getUserCalendar() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(eventService.getEventsForUser(userEmail));
    }

    @GetMapping("/admin/calendar")
    public ResponseEntity<List<EventAdminCalendarDto>> getAdminCalendar() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(eventService.getEventsForAdmin(userEmail));
    }

    @GetMapping("/user-home")
    public ResponseEntity<List<EventHomeWidgetDto>> getUserHome() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(eventService.getUpcomingEventsForUser(userEmail));
    }

    @PostMapping("/admin/add")
    public EventDto create(@RequestBody EventFormDto event) {
        return EventDto.fromEntity(eventService.save(event));
    }

    @PutMapping("/{id}/enroll")
    public ResponseEntity<EventDto> enroll(@PathVariable Integer id) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(EventDto.fromEntity(eventService.enrollStudent(id, userEmail)));
    }

    @PutMapping("/{id}/unenroll")
    public ResponseEntity<EventDto> unenroll(@PathVariable Integer id) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(EventDto.fromEntity(eventService.unenrollStudent(id, userEmail)));
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<EventDto> update(@PathVariable Integer id, @RequestBody EventFormDto event) {
        return ResponseEntity.ok(EventDto.fromEntity(eventService.update(id, event)));
    }

    @PutMapping("/admin/update-status/{id}")
    public ResponseEntity<EventDto> updateStatus(@PathVariable Integer id, @RequestBody String status) {
        return ResponseEntity.ok(EventDto.fromEntity(eventService.updateStatus(id, status)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}