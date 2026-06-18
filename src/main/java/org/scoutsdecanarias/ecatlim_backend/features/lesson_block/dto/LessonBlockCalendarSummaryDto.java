package org.scoutsdecanarias.ecatlim_backend.features.lesson_block.dto;

import org.scoutsdecanarias.ecatlim_backend.entity.LessonBlock;

public record LessonBlockCalendarSummaryDto(
        String name,
        String code,
        Integer hours
) {
    public static LessonBlockCalendarSummaryDto fromEntity(LessonBlock lessonBlock) {
        return new LessonBlockCalendarSummaryDto(
                lessonBlock.getName(),
                lessonBlock.getCode(),
                lessonBlock.getContactHours()
        );
    }
}
