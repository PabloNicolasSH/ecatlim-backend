package org.scoutsdecanarias.ecatlim_backend.features.head_education.dto;

import org.scoutsdecanarias.ecatlim_backend.features.enrollment.UserEnrollmentDetailDto;
import org.scoutsdecanarias.ecatlim_backend.features.event.dto.AttendedEventDto;

import java.util.List;

public record HeadEducationMemberHistoryDto(
        HeadEducationMemberDto member,
        List<UserEnrollmentDetailDto> stages,
        List<StageEventsDto> attendedEvents
) {
    /** Attended events of one education stage, keyed by the stage name used in {@link UserEnrollmentDetailDto#stageName()}. */
    public record StageEventsDto(String stageName, List<AttendedEventDto> events) {
    }
}
