package org.scoutsdecanarias.ecatlim_backend.features.event.service;

import lombok.RequiredArgsConstructor;
import org.scoutsdecanarias.ecatlim_backend.features.event.repository.EventRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("eventSecurity")
@RequiredArgsConstructor
public class EventSecurity {

    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public boolean isDirector(Integer eventId, String userEmail) {
        return eventRepository.findById(eventId)
                .map(event -> event.getDirector() != null && event.getDirector().getEmail().equals(userEmail))
                .orElse(false);
    }
}
