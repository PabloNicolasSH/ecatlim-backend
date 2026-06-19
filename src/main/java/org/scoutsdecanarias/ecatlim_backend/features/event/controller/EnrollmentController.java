package org.scoutsdecanarias.ecatlim_backend.features.event.controller;

import org.scoutsdecanarias.ecatlim_backend.features.education_stage.dto.EducationStageCardDto;
import org.scoutsdecanarias.ecatlim_backend.features.event.service.EnrollmentService;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.UserEnrollmentDetailDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping("/{stageId}/enroll")
    public EducationStageCardDto enroll(@PathVariable Integer stageId) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return enrollmentService.enrollUserInStage(userEmail, stageId);
    }

    @GetMapping("/my-progress")
    public ResponseEntity<List<UserEnrollmentDetailDto>> getMyProgress() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(enrollmentService.getUserProgress(userEmail));
    }
}
