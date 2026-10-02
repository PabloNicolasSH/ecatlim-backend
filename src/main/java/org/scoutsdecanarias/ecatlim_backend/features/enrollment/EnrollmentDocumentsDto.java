package org.scoutsdecanarias.ecatlim_backend.features.enrollment;

import org.scoutsdecanarias.ecatlim_backend.features.education_stage.UserEducationStage;
import org.scoutsdecanarias.ecatlim_backend.features.user_file.UserFile;

import java.time.ZonedDateTime;

public record EnrollmentDocumentsDto(
        EnrollmentDocumentDto personalPlan,
        EnrollmentDocumentDto entityApproval
) {
    public record EnrollmentDocumentDto(Integer fileId, String name, String mimeType, ZonedDateTime uploadDate) {
        static EnrollmentDocumentDto fromEntity(UserFile file) {
            return file == null ? null : new EnrollmentDocumentDto(file.getId(), file.getName(), file.getMimeType(), file.getUploadDate());
        }
    }

    public static EnrollmentDocumentsDto fromEnrollment(UserEducationStage enrollment) {
        return new EnrollmentDocumentsDto(
                EnrollmentDocumentDto.fromEntity(enrollment.getPersonalPlan()),
                EnrollmentDocumentDto.fromEntity(enrollment.getEntityApproval())
        );
    }
}
