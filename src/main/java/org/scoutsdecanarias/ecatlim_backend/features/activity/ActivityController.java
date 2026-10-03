package org.scoutsdecanarias.ecatlim_backend.features.activity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.scoutsdecanarias.ecatlim_backend.features.activity.dto.ActivityCreationDto;
import org.scoutsdecanarias.ecatlim_backend.features.activity.dto.ActivityDto;
import org.scoutsdecanarias.ecatlim_backend.features.activity.dto.FileSubmissionDto;
import org.scoutsdecanarias.ecatlim_backend.features.activity.dto.ForumPublicationDto;
import org.scoutsdecanarias.ecatlim_backend.features.activity.dto.ForumPublicationFormDto;
import org.scoutsdecanarias.ecatlim_backend.features.activity.dto.SurveyResponseDto;
import org.scoutsdecanarias.ecatlim_backend.features.activity.service.ActivityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/activity")
public class ActivityController {
    private final ActivityService activityService;

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<ActivityDto>> getActivitiesByEvent(@PathVariable Integer eventId) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(activityService.getActivitiesByEventForUser(eventId, currentUserEmail));
    }

    @PostMapping("/event/{eventId}")
    @PreAuthorize("hasAnyAuthority('MANAGER_DIRECTOR', 'MANAGEMENT', 'EVENT_DIRECTOR', 'TRAINER')")
    public ResponseEntity<ActivityDto> createActivity(@PathVariable Integer eventId, @Valid @RequestBody ActivityCreationDto activity) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ActivityDto.fromEntity(activityService.createActivity(eventId, activity, currentUserEmail)));
    }

    @GetMapping("/{activityId}/publications")
    public ResponseEntity<List<ForumPublicationDto>> getPublicationsSorted(@PathVariable Integer activityId) {
        return ResponseEntity.ok(ForumPublicationDto.fromCollection(activityService.getPublicationsSorted(activityId)));
    }

    @PostMapping("/{activityId}/forum-publications")
    public ResponseEntity<ForumPublicationDto> publishInForum(@PathVariable Integer activityId, @Valid @RequestBody ForumPublicationFormDto dto) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ForumPublicationDto.fromEntity(activityService.createForumPublication(activityId, currentUserEmail, dto)));
    }

    @PostMapping("/{activityId}/survey-responses")
    public ResponseEntity<Void> submitSurvey(@PathVariable Integer activityId,
                                             @RequestBody @NotEmpty(message = "Debes responder al menos una pregunta") List<@Valid SurveyResponseDto> responses) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        activityService.submitSurveyResponses(activityId, currentUserEmail, responses);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{activityId}/file-submissions")
    public ResponseEntity<FileSubmissionDto> uploadFile(
            @PathVariable Integer activityId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "comment", required = false)
            @Size(max = 2000, message = "El comentario no puede superar los 2000 caracteres") String comment) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FileSubmissionDto.fromEntity(activityService.submitFile(activityId, currentUserEmail, file, comment)));
    }
}
