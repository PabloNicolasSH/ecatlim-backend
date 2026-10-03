package org.scoutsdecanarias.ecatlim_backend.features.activity.dto;

import org.scoutsdecanarias.ecatlim_backend.features.activity.entity.FileSubmission;

import java.time.LocalDateTime;

public record FileSubmissionDto(
        Integer id,
        String comment,
        LocalDateTime submittedAt,
        Boolean isApproved
) {
    public static FileSubmissionDto fromEntity(FileSubmission submission) {
        return new FileSubmissionDto(
                submission.getId(),
                submission.getComment(),
                submission.getSubmittedAt(),
                submission.getIsApproved()
        );
    }
}
