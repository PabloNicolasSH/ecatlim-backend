package org.scoutsdecanarias.ecatlim_backend.features.learning_resource.controller;

import lombok.RequiredArgsConstructor;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.dto.LearningResourceDto;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.dto.LearningResourceUploadDto;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.service.LearningResourceService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/learning-resources")
public class LearningResourceController {

    private final LearningResourceService learningResourceService;

    @GetMapping
    public ResponseEntity<List<LearningResourceDto>> getAllLearningResources() {
        return ResponseEntity.ok(LearningResourceDto.fromCollection(learningResourceService.getAll()));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Integer id) {
        return learningResourceService.downloadResource(id);
    }

    @PreAuthorize("hasAnyAuthority('MANAGEMENT', 'EVENT_DIRECTOR', 'TRAINER', 'ADMIN')")
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LearningResourceDto> upload(@RequestPart(value = "file", required = false) MultipartFile file, @RequestPart("data") LearningResourceUploadDto data) throws IOException {
        return ResponseEntity.ok(LearningResourceDto.fromEntity(learningResourceService.create(data, file)));
    }
}
