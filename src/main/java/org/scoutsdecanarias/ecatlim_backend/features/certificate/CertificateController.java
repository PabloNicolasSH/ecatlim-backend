package org.scoutsdecanarias.ecatlim_backend.features.certificate;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.scoutsdecanarias.ecatlim_backend.features.certificate.dto.AttendanceSummaryDto;
import org.scoutsdecanarias.ecatlim_backend.features.certificate.dto.BlockPassedFormDto;
import org.scoutsdecanarias.ecatlim_backend.features.certificate.dto.SendBlockCertificatesFormDto;
import org.scoutsdecanarias.ecatlim_backend.features.enrollment.EnrollmentDocumentsDto;
import org.scoutsdecanarias.ecatlim_backend.features.enrollment.UserEnrollmentDetailDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/certificates")
public class CertificateController {

    private final CertificateService certificateService;

    @GetMapping("/blocks/{lessonBlockId}/download")
    public ResponseEntity<byte[]> downloadBlockCertificate(@PathVariable Integer lessonBlockId) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return certificateService.downloadBlockCertificate(userEmail, lessonBlockId);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGEMENT', 'MANAGER_DIRECTOR')")
    @GetMapping("/users/{userId}/progress")
    public ResponseEntity<List<UserEnrollmentDetailDto>> getUserProgress(@PathVariable Integer userId) {
        return ResponseEntity.ok(certificateService.getUserProgress(userId));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGEMENT', 'MANAGER_DIRECTOR')")
    @PostMapping("/users/{userId}/attendance/send")
    public ResponseEntity<AttendanceSummaryDto> sendAttendanceCertificate(@PathVariable Integer userId,
                                                                          @RequestParam(required = false) Integer stageId) {
        return ResponseEntity.ok(certificateService.sendAttendanceCertificate(userId, stageId));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGEMENT', 'MANAGER_DIRECTOR')")
    @PutMapping("/users/{userId}/blocks/{lessonBlockId}/passed")
    public ResponseEntity<Void> setBlockPassed(@PathVariable Integer userId, @PathVariable Integer lessonBlockId,
                                               @Valid @RequestBody BlockPassedFormDto form) {
        certificateService.setBlockPassed(userId, lessonBlockId, form.passed());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGEMENT', 'MANAGER_DIRECTOR')")
    @PostMapping("/users/{userId}/blocks/send")
    public ResponseEntity<Void> sendBlockCertificates(@PathVariable Integer userId,
                                                      @Valid @RequestBody SendBlockCertificatesFormDto form) {
        certificateService.sendBlockCertificates(userId, form.lessonBlockIds());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGEMENT', 'MANAGER_DIRECTOR')")
    @PostMapping(value = "/stages/{enrollmentId}/certificate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EnrollmentDocumentsDto> uploadStageCertificate(@PathVariable Integer enrollmentId,
                                                                         @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(certificateService.uploadStageCertificate(enrollmentId, file));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGEMENT', 'MANAGER_DIRECTOR')")
    @PostMapping("/stages/{enrollmentId}/complete")
    public ResponseEntity<Void> completeStage(@PathVariable Integer enrollmentId) {
        certificateService.completeStage(enrollmentId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGEMENT', 'MANAGER_DIRECTOR')")
    @PostMapping("/stages/{enrollmentId}/send")
    public ResponseEntity<Void> sendStageCertificate(@PathVariable Integer enrollmentId) {
        certificateService.sendStageCertificate(enrollmentId);
        return ResponseEntity.noContent().build();
    }
}
