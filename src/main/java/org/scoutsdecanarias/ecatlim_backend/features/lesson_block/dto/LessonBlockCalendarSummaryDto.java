package org.scoutsdecanarias.ecatlim_backend.features.lesson_block.dto;

import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.LessonBlock;

public record LessonBlockCalendarSummaryDto(
        Integer id,
        String name,
        String code,
        Integer hours
) {
    public static LessonBlockCalendarSummaryDto fromEntity(LessonBlock lessonBlock) {
        return new LessonBlockCalendarSummaryDto(
                lessonBlock.getId(),
                lessonBlock.getName(),
                lessonBlock.getCode(),
                lessonBlock.getContactHours()
        );
    }
}
