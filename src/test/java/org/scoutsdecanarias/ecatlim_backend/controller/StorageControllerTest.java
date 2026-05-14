package org.scoutsdecanarias.ecatlim_backend.controller;

import org.scoutsdecanarias.ecatlim_backend.dto.UploadResponse;
import org.scoutsdecanarias.ecatlim_backend.enums.BlobDirectory;
import org.scoutsdecanarias.ecatlim_backend.service.BlobStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/test/storage")
public class StorageTestController {

    private final BlobStorageService blobStorageService;

    public StorageTestController(BlobStorageService blobStorageService) {
        this.blobStorageService = blobStorageService;
    }

    @PostMapping("/upload-profile")
    public ResponseEntity<UploadResponse> testUpload(@RequestParam("file") MultipartFile file) {
        try {
            UploadResponse result = blobStorageService.upload(
                    file,
                    BlobDirectory.PROFILE_PHOTOS,
                    "test-scout-" + System.currentTimeMillis() + ".jpg"
            );
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
