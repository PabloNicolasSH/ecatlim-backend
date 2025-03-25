package org.scoutsdecanarias.ecatlim_backend.service;

import org.scoutsdecanarias.ecatlim_backend.entity.Event;
import org.scoutsdecanarias.ecatlim_backend.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> getEvents() {
        return eventRepository.findAll();
    }

    public Event getEventById(Integer id) {
        return eventRepository.findById(id).orElseThrow(NoSuchElementException::new);
    }

    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    public Event updateEvent(Integer id, Event event) {
        Event updatedEvent = eventRepository.findById(id).orElseThrow(NoSuchElementException::new);

        updatedEvent.setDirector(event.getDirector());
        updatedEvent.setOrganizer(event.getOrganizer());
        updatedEvent.setTitle(event.getTitle());
        updatedEvent.setLocation(event.getLocation());
        updatedEvent.setStartDate(event.getStartDate());
        updatedEvent.setEndDate(event.getEndDate());
        updatedEvent.setTheoreticalHours(event.getTheoreticalHours());
        updatedEvent.setPracticalHours(event.getPracticalHours());
        updatedEvent.setOnlineHours(event.getOnlineHours());

        return eventRepository.save(updatedEvent);
    }
}
