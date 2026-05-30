package org.scoutsdecanarias.ecatlim_backend.admin_dashboard.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DashboardDto(
        Integer activeStudents,
        Integer studentGrowthPercentage,
        String nextEventName,
        LocalDateTime nextEventDate,
        Integer averageGrade,
        String topModule,
        Integer schoolCapacityPercentage,
        String activeTrainersRatio,
        List<EventSummaryDto> upcomingEvents
) {
}
