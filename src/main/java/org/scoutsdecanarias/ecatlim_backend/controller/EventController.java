package org.scoutsdecanarias.ecatlim_backend.controller;

import org.scoutsdecanarias.ecatlim_backend.dto.event.EventDto;
import org.scoutsdecanarias.ecatlim_backend.dto.event.EventFormDto;
import org.scoutsdecanarias.ecatlim_backend.dto.event.EventHomeWidgetDto;
import org.scoutsdecanarias.ecatlim_backend.dto.event.EventUserCalendarDto;
import org.scoutsdecanarias.ecatlim_backend.entity.Event;
import org.scoutsdecanarias.ecatlim_backend.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/{id}")
    public ResponseEntity<EventDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(EventDto.fromEntity(eventService.findById(id)));
    }

    @GetMapping("/user-calendar")
    public ResponseEntity<List<EventUserCalendarDto>> getUserCalendar() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(eventService.getEventsForUser(userEmail));
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

    @PutMapping("/{id}")
    public ResponseEntity<EventDto> update(@PathVariable Integer id, @RequestBody EventFormDto event) {
        return ResponseEntity.ok(EventDto.fromEntity(eventService.update(id, event)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}