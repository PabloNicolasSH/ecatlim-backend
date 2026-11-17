package org.scoutsdecanarias.ecatlim_backend.features.event.dto;

public record StudentUnenrolledEvent(Integer eventId, Integer studentId, Integer lessonBlockId) {
}
