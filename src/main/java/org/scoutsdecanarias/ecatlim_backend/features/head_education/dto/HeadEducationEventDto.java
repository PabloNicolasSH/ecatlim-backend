package org.scoutsdecanarias.ecatlim_backend.features.head_education.dto;

import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.dto.LessonBlockCalendarSummaryDto;

import java.time.LocalDateTime;
import java.util.List;

public record HeadEducationEventDto(
        Integer id,
        String title,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String location,
        LocalDateTime inscriptionCloseDate,
        boolean inscriptionClosed,
        String transferBankNumber,
        String transferCode,
        Integer costPerPerson,
        int totalAmount,
        int paidAmount,
        int pendingAmount,
        List<AttendeeDto> attendees
) {
    public record AttendeeDto(
            Integer userId,
            String name,
            String surname,
            String email,
            String avatarUrl,
            String paymentStatus,
            List<LessonBlockCalendarSummaryDto> lessonBlocks
    ) {
    }
}
