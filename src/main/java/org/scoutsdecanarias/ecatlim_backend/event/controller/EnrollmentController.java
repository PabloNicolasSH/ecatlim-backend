package org.scoutsdecanarias.ecatlim_backend.event.controller;

import org.scoutsdecanarias.ecatlim_backend.dto.education_stage.EducationStageCardDto;
import org.scoutsdecanarias.ecatlim_backend.dto.user.UserEnrollmentDetailDto;
import org.scoutsdecanarias.ecatlim_backend.event.service.EnrollmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
