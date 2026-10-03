package org.scoutsdecanarias.ecatlim_backend.features.learning_resource.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.dto.LearningResourceDto;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.dto.LearningResourceUploadDto;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.entity.LearningResource;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.service.LearningResourceService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/learning-resources")
public class LearningResourceController {

    private final LearningResourceService learningResourceService;

    @GetMapping
    public ResponseEntity<List<LearningResourceDto>> getAllLearningResources() {
        return ResponseEntity.ok(LearningResourceDto.fromCollection(learningResourceService.getAll(), learningResourceService::canManage));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Integer id) {
        return learningResourceService.downloadResource(id);
    }

    @GetMapping("/{id}/thumbnail")
    public ResponseEntity<byte[]> thumbnail(@PathVariable Integer id) {
        return learningResourceService.thumbnail(id);
    }

    @PostMapping("/{id}/open")
    public ResponseEntity<Void> registerOpen(@PathVariable Integer id) {
        learningResourceService.registerOpen(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('MANAGEMENT', 'EVENT_DIRECTOR', 'TRAINER', 'ADMIN')")
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LearningResourceDto> upload(@RequestPart(value = "file", required = false) MultipartFile file, @Valid @RequestPart("data") LearningResourceUploadDto data) throws IOException {
        log.info("METHOD upload() - Uploading learning resource {}", data.name());
        return ResponseEntity.ok(toDto(learningResourceService.create(data, file)));
    }

    @PreAuthorize("hasAnyAuthority('MANAGEMENT', 'EVENT_DIRECTOR', 'TRAINER', 'ADMIN')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LearningResourceDto> update(@PathVariable Integer id, @RequestPart(value = "file", required = false) MultipartFile file, @Valid @RequestPart("data") LearningResourceUploadDto data) throws IOException {
        log.info("METHOD update() - Updating learning resource {}", id);
        return ResponseEntity.ok(toDto(learningResourceService.update(id, data, file)));
    }

    @PreAuthorize("hasAnyAuthority('MANAGEMENT', 'EVENT_DIRECTOR', 'TRAINER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        log.info("METHOD delete() - Deleting learning resource {}", id);
        learningResourceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private LearningResourceDto toDto(LearningResource resource) {
        return LearningResourceDto.fromEntity(resource, learningResourceService.canManage(resource));
    }
}
