package org.scoutsdecanarias.ecatlim_backend.features.event.dto;

public record StudentEnrolledEvent(Integer eventId, Integer studentId, Integer lessonBlockId) {
}
