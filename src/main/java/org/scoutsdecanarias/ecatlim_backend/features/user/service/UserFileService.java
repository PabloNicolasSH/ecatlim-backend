package org.scoutsdecanarias.ecatlim_backend.features.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.scoutsdecanarias.ecatlim_backend.dto.UploadResponse;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.UserProfile;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.scoutsdecanarias.ecatlim_backend.shared.blob.BlobDirectory;
import org.scoutsdecanarias.ecatlim_backend.shared.blob.BlobStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserFileService {
    private final BlobStorageService blobStorageService;
    private final UserRepository userRepository;

    @Transactional
    public String uploadProfileAvatar(User user, MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String fileName = "avatar-" + user.getId() + "-" + UUID.randomUUID().toString().substring(0, 8) + extension;

            UploadResponse response = blobStorageService.upload(file, BlobDirectory.PROFILE_PHOTOS, fileName);

            UserProfile userProfile = user.getProfile();
            userProfile.setProfilePictureUrl(response.thumbnailUrl());
            user.setProfile(userProfile);
            userRepository.save(user);

            return response.thumbnailUrl();
        } catch (IOException e) {
            throw new RuntimeException("Error al procesar la imagen en el almacenamiento de Azure", e);
        }
    }

}
