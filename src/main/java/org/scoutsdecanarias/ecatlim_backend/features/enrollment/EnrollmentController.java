package org.scoutsdecanarias.ecatlim_backend.features.enrollment;

import lombok.RequiredArgsConstructor;
import org.scoutsdecanarias.ecatlim_backend.features.education_stage.dto.EducationStageCardDto;
import org.scoutsdecanarias.ecatlim_backend.features.event.dto.EventDto;
import org.scoutsdecanarias.ecatlim_backend.features.event.service.EnrollmentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/stages/{stageId}/enroll")
    public EducationStageCardDto stageEnroll(@PathVariable Integer stageId) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return enrollmentService.enrollUserInStage(userEmail, stageId);
    }

    @GetMapping("/stages/my-progress")
    public ResponseEntity<List<UserEnrollmentDetailDto>> getMyProgress() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(enrollmentService.getUserProgress(userEmail));
    }

    @PostMapping(value = "/stages/{enrollmentId}/documents/{type}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EnrollmentDocumentsDto> uploadDocument(@PathVariable Integer enrollmentId,
                                                                 @PathVariable EnrollmentDocumentType type,
                                                                 @RequestParam("file") MultipartFile file) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(enrollmentService.uploadDocument(userEmail, enrollmentId, type, file));
    }

    @PostMapping("/events/{id}/enroll")
    public ResponseEntity<EventDto> eventEnroll(@PathVariable Integer id, @RequestBody List<Integer> lessonBlockIds) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(EventDto.fromEntity(enrollmentService.enrollStudent(id, userEmail, lessonBlockIds)));
    }

    @PostMapping("/events/{id}/unenroll")
    public ResponseEntity<EventDto> eventUnenroll(@PathVariable Integer id, @RequestBody List<Integer> lessonBlockIds) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(EventDto.fromEntity(enrollmentService.unenrollStudent(id, userEmail, lessonBlockIds)));
    }
}
