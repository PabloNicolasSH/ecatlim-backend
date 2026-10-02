package org.scoutsdecanarias.ecatlim_backend.features.event.dto;

import org.scoutsdecanarias.ecatlim_backend.features.activity.dto.ActivityDto;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.dto.LessonBlockCalendarSummaryDto;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.SimpleUserDto;

import java.time.LocalDateTime;
import java.util.List;

public record EventDetailDto(
        Integer id,
        String title,
        String shortName,
        String description,
        String contents,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String location,
        String organizer,
        String status,
        Integer theoreticalHours,
        Integer practicalHours,
        Integer onlineHours,
        StageDto educationStage,
        SimpleUserDto director,
        List<SimpleUserDto> facilitators,
        List<SimpleUserDto> staff,
        List<LessonBlockCalendarSummaryDto> lessonBlocks,
        ConfigDto config,
        List<ParticipantDto> participants,
        List<TimelineEntryDto> timeline,
        List<ActivityDto> activities
) {
    public record StageDto(Integer id, String name, String code) {
    }

    public record ConfigDto(
            Integer minParticipants,
            LocalDateTime dateOpenInscription,
            LocalDateTime dateCloseInscription,
            Integer cost,
            String transferBankNumber,
            String transferCode,
            List<String> notificationTargets
    ) {
    }

    public record ParticipantDto(
            Integer userId,
            String name,
            String surname,
            String email,
            String avatarUrl,
            String entityName,
            String paymentStatus,
            List<ParticipantBlockDto> blocks
    ) {
    }

    public record ParticipantBlockDto(String code, String name, boolean attended) {
    }

    public record TimelineEntryDto(
            Integer id,
            String title,
            String description,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String type
    ) {
    }
}
