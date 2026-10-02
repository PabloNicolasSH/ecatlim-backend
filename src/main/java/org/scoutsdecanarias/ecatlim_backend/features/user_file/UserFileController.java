package org.scoutsdecanarias.ecatlim_backend.features.user_file;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me/files")
public class UserFileController {
    private final UserFileService userFileService;

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadAvatar(@RequestParam(value = "file", required = false) @Nullable MultipartFile file) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        userFileService.uploadProfileAvatar(currentUserEmail, file);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<byte[]> getUserFile(@PathVariable Integer fileId) {
        return userFileService.downloadUserFile(fileId);
    }

    @GetMapping("thumbnail/{fileId}")
    public ResponseEntity<byte[]> getUserFileThumbnail(@PathVariable Integer fileId) {
        return userFileService.downloadUserFileThumbnail(fileId);
    }
}
