package org.scoutsdecanarias.ecatlim_backend.features.activity;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.scoutsdecanarias.ecatlim_backend.features.activity.dto.*;
import org.scoutsdecanarias.ecatlim_backend.features.activity.entity.Activity;
import org.scoutsdecanarias.ecatlim_backend.features.activity.entity.FileSubmission;
import org.scoutsdecanarias.ecatlim_backend.features.activity.entity.ForumPublication;
import org.scoutsdecanarias.ecatlim_backend.features.activity.service.ActivityService;
import org.scoutsdecanarias.ecatlim_backend.shared.blob.BlobDirectory;
import org.scoutsdecanarias.ecatlim_backend.shared.blob.BlobStorageService;
import org.scoutsdecanarias.ecatlim_backend.shared.blob.UploadResponse;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/activity")
public class ActivityController {
    private final ActivityService activityService;
    private final BlobStorageService blobStorageService;

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<ActivityDto>> getActivitiesByEvent(@PathVariable Integer eventId) {
        return ResponseEntity.ok(ActivityDto.fromCollection(activityService.getActivitiesByEvent(eventId)));
    }

    @PostMapping("/event/{eventId}")
    public ResponseEntity<ActivityDto> createActivity(@PathVariable Integer eventId, @Valid @RequestBody ActivityCreationDto activity) {
        return ResponseEntity.ok(ActivityDto.fromEntity(activityService.createActivity(eventId, activity)));
    }

    @GetMapping("/{activityId}/publications")
    public ResponseEntity<List<ForumPublication>> getPublicationsSorted(@PathVariable Integer activityId) {
        return ResponseEntity.ok(activityService.getPublicationsSorted(activityId));
    }

    @PostMapping("/{activityId}/forum-publications")
    public ResponseEntity<ForumPublication> publishInForum(@PathVariable Integer activityId, @Valid @RequestBody ForumPublicationDto dto) {
        ForumPublication publication = activityService.createForumPublication(activityId, dto.studentId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(publication);
    }

    @PostMapping("/{activityId}/survey-responses")
    public ResponseEntity<Void> submitSurvey(@PathVariable Integer activityId, @RequestParam Integer studentId, @Valid @RequestBody List<SurveyResponseDto> responses) {
        activityService.submitSurveyResponses(activityId, studentId, responses);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{activityId}/file-submissions")
    public ResponseEntity<FileSubmission> uploadFile(
            @PathVariable Integer activityId,
            @RequestParam Integer studentId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "comment", required = false) String comment) throws IOException {

        String customFileName = studentId + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();

        UploadResponse uploadResponse = blobStorageService.upload(file, BlobDirectory.ACTIVITY_ATTACHMENTS, customFileName);

        String fileUrl = uploadResponse.url();

        FileSubmission submission = activityService.submitFile(activityId, studentId, fileUrl, comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(submission);
    }
}
