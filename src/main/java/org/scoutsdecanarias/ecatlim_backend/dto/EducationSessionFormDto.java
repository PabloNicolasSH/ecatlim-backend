package org.scoutsdecanarias.ecatlim_backend.dto;

import org.scoutsdecanarias.ecatlim_backend.entity.EducationSession;
import org.scoutsdecanarias.ecatlim_backend.entity.User;

import java.util.List;

public record EducationSessionFormDto(
        Integer lessonBlockId,
        List<Integer> trainerIds
) {
    public static EducationSessionFormDto fromEntity(EducationSession educationSession) {
        return new EducationSessionFormDto(
                educationSession.getLessonBlock().getId(),
                educationSession.getFacilitators().stream().map(User::getId).toList()
        );
    }
}
