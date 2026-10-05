package org.scoutsdecanarias.ecatlim_backend.features.recognition.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.scoutsdecanarias.ecatlim_backend.features.recognition.dto.RecognitionCommissionFormDto;
import org.scoutsdecanarias.ecatlim_backend.features.recognition.dto.RecognitionFormDto;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.SimpleUserDto;
import org.scoutsdecanarias.ecatlim_backend.features.recognition.dto.RecognitionRequestDto;
import org.scoutsdecanarias.ecatlim_backend.features.recognition.dto.RecognitionResponseFormDto;
import org.scoutsdecanarias.ecatlim_backend.features.recognition.enums.RecognitionStatus;
import org.scoutsdecanarias.ecatlim_backend.features.recognition.service.RecognitionService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/recognitions")
public class RecognitionController {

    private static final String MANAGERS = "hasAnyAuthority('MANAGEMENT', 'MANAGER_DIRECTOR')";
    private static final String STAFF = "hasAnyAuthority('MANAGEMENT', 'MANAGER_DIRECTOR', 'EVENT_DIRECTOR', 'TRAINER')";

    private final RecognitionService recognitionService;

    @GetMapping("/mine")
    public List<RecognitionRequestDto> getMyRequests() {
        return recognitionService.getMyRequests(currentEmail());
    }

    @PostMapping(value = "/blocks/{lessonBlockId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RecognitionRequestDto openRequest(@PathVariable Integer lessonBlockId,
                                             @RequestPart("data") @Valid RecognitionFormDto data,
                                             @RequestPart(value = "files", required = false) @Nullable List<MultipartFile> files) {
        log.info("METHOD openRequest() - Opening recognition request for block {}", lessonBlockId);
        return recognitionService.openRequest(currentEmail(), lessonBlockId, data, files);
    }

    @PostMapping(value = "/{id}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RecognitionRequestDto addDocumentation(@PathVariable Integer id,
                                                  @RequestParam(value = "comment", required = false) @Nullable String comment,
                                                  @RequestPart(value = "files", required = false) @Nullable List<MultipartFile> files) {
        log.info("METHOD addDocumentation() - Adding documentation to recognition request {}", id);
        return recognitionService.addDocumentation(currentEmail(), id, comment, files);
    }

    @PreAuthorize(STAFF)
    @GetMapping("/admin")
    public List<RecognitionRequestDto> getRequestsForReview(@RequestParam(value = "status", required = false) @Nullable Set<RecognitionStatus> status) {
        return recognitionService.getRequestsForReview(currentEmail(), status);
    }

    @PreAuthorize(STAFF)
    @GetMapping("/admin/{id}")
    public RecognitionRequestDto getRequest(@PathVariable Integer id) {
        return recognitionService.getRequest(currentEmail(), id);
    }

    @PreAuthorize(MANAGERS)
    @GetMapping("/admin/commission-candidates")
    public List<SimpleUserDto> getCommissionCandidates() {
        return recognitionService.getCommissionCandidates();
    }

    @PreAuthorize(MANAGERS)
    @PutMapping("/admin/{id}/commission")
    public RecognitionRequestDto assignCommission(@PathVariable Integer id, @RequestBody @Valid RecognitionCommissionFormDto form) {
        log.info("METHOD assignCommission() - Assigning commission to recognition request {}", id);
        return recognitionService.assignCommission(id, form.userIds());
    }

    /** Only the members of the request's commission can answer; the service enforces it. */
    @PreAuthorize(STAFF)
    @PostMapping("/admin/{id}/respond")
    public RecognitionRequestDto respond(@PathVariable Integer id, @RequestBody @Valid RecognitionResponseFormDto form) {
        log.info("METHOD respond() - Responding {} to recognition request {}", form.decision(), id);
        return recognitionService.respond(currentEmail(), id, form);
    }

    private String currentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
