package org.scoutsdecanarias.ecatlim_backend.admin_dashboard.dto;

public record EventSummaryDto(
        String title,
        String dateRange,
        Integer totalAttendees
) {}
