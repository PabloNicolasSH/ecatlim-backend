package org.scoutsdecanarias.ecatlim_backend.features.activity.dto;

import java.time.LocalDateTime;

public record PendingActivityRow(Integer studentId, String activityTitle, LocalDateTime dueDate, String eventTitle) {
}
