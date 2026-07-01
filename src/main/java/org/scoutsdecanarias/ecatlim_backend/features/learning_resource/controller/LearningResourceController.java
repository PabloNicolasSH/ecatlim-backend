package org.scoutsdecanarias.ecatlim_backend.features.learning_resource.controller;

import lombok.RequiredArgsConstructor;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.dto.LearningResourceDto;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.dto.LearningResourceUploadDto;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.entity.LearningResource;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.service.LearningResourceService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LearningResource> upload(@RequestPart(value = "file") MultipartFile file, @RequestPart("data") LearningResourceUploadDto data) throws IOException {
        return ResponseEntity.ok(learningResourceService.create(data, file));
    }
}
