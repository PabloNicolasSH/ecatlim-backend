package org.scoutsdecanarias.ecatlim_backend.admin_dashboard;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.scoutsdecanarias.ecatlim_backend.admin_dashboard.dto.DashboardDto;
import org.scoutsdecanarias.ecatlim_backend.admin_dashboard.dto.EventSummaryDto;
import org.scoutsdecanarias.ecatlim_backend.entity.Event;
import org.scoutsdecanarias.ecatlim_backend.enums.Role;
import org.scoutsdecanarias.ecatlim_backend.repository.EventRepository;
import org.scoutsdecanarias.ecatlim_backend.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public DashboardDto getDashboardSummary() {

        Integer activeStudents = userRepository.countUsersByEnabledAndRole(true, Role.STUDENT);

        List<Event> upcomingEvents = eventRepository.findUpcomingEvents(
                LocalDateTime.now(),
                PageRequest.of(0, 4)
        );

        Event nextEvent = upcomingEvents.getFirst();
        upcomingEvents.remove(nextEvent);

        return new DashboardDto(
                activeStudents,
                12,
                nextEvent.getTitle(),
                nextEvent.getStartDate(),
                7,
                EventSummaryDto.fromCollection(upcomingEvents)
        );
    }
}
