package org.scoutsdecanarias.ecatlim_backend.features.user_file;

import com.azure.core.annotation.Get;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me/files")
public class UserFileController {
    private final UserFileService userFileService;

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadAvatar(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

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
