package org.scoutsdecanarias.ecatlim_backend.features.user_file;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.scoutsdecanarias.ecatlim_backend.dto.UploadResponse;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.UserProfile;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.scoutsdecanarias.ecatlim_backend.shared.blob.BlobDirectory;
import org.scoutsdecanarias.ecatlim_backend.shared.blob.BlobStorageService;
import org.scoutsdecanarias.ecatlim_backend.shared.utils.FileTransferDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserFileService {

    private final BlobStorageService blobStorageService;
    private final UserRepository userRepository;
    private final UserFileRepository userFileRepository;

    @Transactional
    public void uploadProfileAvatar(String email, MultipartFile file) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el email: " + email));

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String customName = "profile_" + user.getProfile().getName() +"." + extension;

            String fileUuid = UUID.randomUUID().toString();
            String blobFileName = fileUuid + extension;

            blobStorageService.upload(file, BlobDirectory.PROFILE_PHOTOS, blobFileName);

            UserFile userFile = new UserFile();
            userFile.setUuid(fileUuid);
            userFile.setName(originalFilename);
            userFile.setFileType(UserFileType.PROFILE);
            userFile.setMimeType(file.getContentType() != null ? file.getContentType() : "image/jpeg");
            userFile.setCustomName(customName);
            userFile.setUploadDate(ZonedDateTime.now());

            UserProfile userProfile = user.getProfile();
            userProfile.setProfilePicture(userFile);
            user.setProfile(userProfile);

            userRepository.save(user);
        } catch (IOException e) {
            throw new RuntimeException("Error al procesar la imagen en el almacenamiento de Azure", e);
        }
    }

    public ResponseEntity<byte[]> downloadUserFile(Integer id) {
        UserFile file = userFileRepository.get(id);

        BlobDirectory directory = getFileTypeDirectory(file.getFileType());
        String blobName = file.getUuid() + file.getName().substring(file.getName().lastIndexOf("."));

        byte[] fileBytes = blobStorageService.download(blobName, directory);

        return new FileTransferDto(fileBytes, file.getName(), file.getMimeType()).asResponseEntity();
    }

    public ResponseEntity<byte[]> downloadUserFileThumbnail(Integer id) {
        UserFile file = userFileRepository.get(id);

        BlobDirectory directory = getFileTypeDirectory(file.getFileType());
        String blobName = file.getUuid() + file.getName().substring(file.getName().lastIndexOf("."));
        byte[] thumbnailBytes = blobStorageService.downloadThumbnail(blobName, directory);

        return new FileTransferDto(thumbnailBytes, file.getName(), file.getMimeType()).asResponseEntity();
    }

    private BlobDirectory getFileTypeDirectory(UserFileType fileType) {
        return switch (fileType) {
            case PROFILE -> BlobDirectory.PROFILE_PHOTOS;
            case USER_EDUCATION_STAGE -> BlobDirectory.EDUCATION_DOCUMENTS;
            case USER_ACTIVITIES -> BlobDirectory.ACTIVITY_ATTACHMENTS;
        };
    }
}
