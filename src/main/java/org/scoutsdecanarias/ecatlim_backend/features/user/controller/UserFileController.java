package org.scoutsdecanarias.ecatlim_backend.features.user.controller;

import lombok.RequiredArgsConstructor;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.service.UserFileService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<Void> uploadAvatar(
            @AuthenticationPrincipal User currentUser,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        userFileService.uploadProfileAvatar(currentUser, file);
        return ResponseEntity.ok().build();
    }
}
