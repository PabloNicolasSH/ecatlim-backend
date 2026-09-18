package org.scoutsdecanarias.ecatlim_backend.admin_dashboard.dto;


import org.scoutsdecanarias.ecatlim_backend.features.event.entity.Event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public record EventSummaryDto(
        String title,
        String dateRange,
        Integer totalAttendees
) {
    private static final Locale LOCALE = new Locale("es", "ES");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM", LOCALE);
    private static final DateTimeFormatter SINGLE_DAY_FORMATTER = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", LOCALE);

    public static EventSummaryDto fromEntity(Event event) {
        return new EventSummaryDto(
                event.getTitle(),
                calculateDateRange(event.getStartDate(), event.getEndDate()),
                event.getEnrolledUsers() != null ? event.getEnrolledUsers().size() : 0
        );
    }

    public static List<EventSummaryDto> fromCollection(List<Event> upcomingEvents) {
        if (upcomingEvents == null) {
            return Collections.emptyList();
        }
        return upcomingEvents.stream()
                .map(EventSummaryDto::fromEntity)
                .collect(Collectors.toList());
    }

    private static String calculateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null) return "-";

        if (end == null || start.toLocalDate().isEqual(end.toLocalDate())) {
            String formatted = start.format(SINGLE_DAY_FORMATTER);
            return formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
        }

        String startDay = String.valueOf(start.getDayOfMonth());
        String endDay = String.valueOf(end.getDayOfMonth());
        String endMonth = end.format(MONTH_FORMATTER);

        endMonth = endMonth.substring(0, 1).toUpperCase() + endMonth.substring(1);

        return String.format("%s - %s de %s", startDay, endDay, endMonth);
    }
}
