package org.scoutsdecanarias.ecatlim_backend.dto;

import org.scoutsdecanarias.ecatlim_backend.entity.LessonBlock;

import java.util.List;
import java.util.stream.Collectors;

public record LessonBlockDto(
        String name,
        int lessonBlockId,
        String description,
        Integer onlineHours,
        Integer contactHours,
        boolean recognizable,
        Integer moduleId
) {
    public static LessonBlockDto fromEntity(LessonBlock lessonBlock) {
        return new LessonBlockDto(
                lessonBlock.getName(),
                lessonBlock.getLessonBlockId(),
                lessonBlock.getDescription(),
                lessonBlock.getOnlineHours(),
                lessonBlock.getContactHours(),
                lessonBlock.isRecognizable(),
                lessonBlock.getModule().getId()
        );
    }

    public static List<LessonBlockDto> fromCollections(List<LessonBlock> lessonBlocks) {
        return lessonBlocks.stream().map(LessonBlockDto::fromEntity).collect(Collectors.toList());
    }
}
