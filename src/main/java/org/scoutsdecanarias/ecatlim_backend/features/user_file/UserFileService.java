package org.scoutsdecanarias.ecatlim_backend.features.user_file;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.scoutsdecanarias.ecatlim_backend.core.exception.ResourceNotFoundException;
import org.scoutsdecanarias.ecatlim_backend.features.education_stage.UserEducationStage;
import org.scoutsdecanarias.ecatlim_backend.features.scout_group.ScoutGroup;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.UserProfile;
import org.scoutsdecanarias.ecatlim_backend.features.user.enums.Role;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserEducationStageRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.scoutsdecanarias.ecatlim_backend.shared.blob.BlobDirectory;
import org.scoutsdecanarias.ecatlim_backend.shared.blob.BlobStorageService;
import org.scoutsdecanarias.ecatlim_backend.shared.utils.FileTransferDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserFileService {

    private final BlobStorageService blobStorageService;
    private final UserRepository userRepository;
    private final UserFileRepository userFileRepository;
    private final UserEducationStageRepository userEducationStageRepository;

    @Transactional
    public void uploadProfileAvatar(String email, @Nullable MultipartFile file) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con el email: " + email));

        //fixme: if how the blobFileName is calculated changes in any way, old profile pictures will not be able to be deleted or accessed.
        // Consider storing the blobFileName in the UserFile entity to avoid this issue. Or just use the UUID for storing
        // this fix will require a migration to update the existing UserFile entities with the correct blobFileName.

        try {
            if (file == null || file.isEmpty()) {
                UserProfile userProfile = user.getProfile();

                if (userProfile.getProfilePicture() != null) {
                    UserFile existingFile = userProfile.getProfilePicture();
                    String extension = existingFile.getName() == null ? ".jpg" : existingFile.getName().substring(existingFile.getName().lastIndexOf("."));
                    String blobName = existingFile.getUuid() + extension;

                    blobStorageService.delete(blobName, BlobDirectory.PROFILE_PHOTOS);
                    userProfile.setProfilePicture(null);
                }
                return;
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String customName = "profile_" + user.getProfile().getName() + "." + extension;

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

    @Transactional
    public ResponseEntity<byte[]> downloadUserFile(Integer id, String requesterEmail) {
        UserFile file = userFileRepository.get(id);
        if (file == null) {
            throw new ResourceNotFoundException("Archivo no encontrado");
        }
        assertCanAccess(file, requesterEmail);

        BlobDirectory directory = getFileTypeDirectory(file.getFileType());
        String blobName = file.getUuid() + file.getName().substring(file.getName().lastIndexOf("."));

        byte[] fileBytes = blobStorageService.download(blobName, directory);

        return new FileTransferDto(fileBytes, file.getName(), file.getMimeType()).asResponseEntity();
    }

    private void assertCanAccess(UserFile file, String requesterEmail) {
        if (file.getFileType() != UserFileType.USER_EDUCATION_STAGE) {
            return;
        }

        Optional<UserEducationStage> asPlan = userEducationStageRepository.findByPersonalPlanId(file.getId());
        Optional<UserEducationStage> asApproval = asPlan.isPresent()
                ? Optional.empty()
                : userEducationStageRepository.findByEntityApprovalId(file.getId());

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new EcatlimException("Usuario no encontrado", HttpStatus.FORBIDDEN));

        boolean allowed = asPlan.map(e -> isStudentOrTutor(e, requester)).orElse(false)
                || asApproval.map(e -> canSeeEntityApproval(e, requester)).orElse(false);

        if (!allowed) {
            throw new EcatlimException("No tienes permiso para ver este archivo", HttpStatus.FORBIDDEN);
        }
    }

    private boolean isStudentOrTutor(UserEducationStage enrollment, User requester) {
        User student = enrollment.getUser();
        if (student.getId().equals(requester.getId())) {
            return true;
        }
        User tutor = enrollment.getTutor();
        return tutor != null && tutor.getId().equals(requester.getId());
    }

    private boolean canSeeEntityApproval(UserEducationStage enrollment, User requester) {
        User student = enrollment.getUser();
        if (student.getId().equals(requester.getId())
                || requester.getRoles().contains(Role.MANAGEMENT)
                || requester.getRoles().contains(Role.MANAGER_DIRECTOR)) {
            return true;
        }
        if (!requester.getRoles().contains(Role.HEAD_OF_EDUCATION)) {
            return false;
        }
        ScoutGroup group = student.getProfile().getScoutGroup();
        return group != null
                && group.getHeadOfEducation() != null
                && group.getHeadOfEducation().getId().equals(requester.getId());
    }

    public ResponseEntity<byte[]> downloadUserFileThumbnail(Integer id) {
        UserFile file = userFileRepository.get(id);

        BlobDirectory directory = getFileTypeDirectory(file.getFileType());
        String blobName = file.getUuid() + file.getName().substring(file.getName().lastIndexOf("."));
        byte[] thumbnailBytes = blobStorageService.downloadThumbnail(blobName, directory);

        return new FileTransferDto(thumbnailBytes, file.getName(), file.getMimeType()).asResponseEntity();
    }

    public UserFile storeFile(MultipartFile file, UserFileType type, String customName) {
        String originalFilename = file.getOriginalFilename();
        int dot = originalFilename == null ? -1 : originalFilename.lastIndexOf(".");
        String extension = dot >= 0 ? originalFilename.substring(dot) : ".jpg";
        String name = dot >= 0 ? originalFilename : "file" + extension;

        String fileUuid = UUID.randomUUID().toString();

        try {
            blobStorageService.upload(file, getFileTypeDirectory(type), fileUuid + extension);
        } catch (IOException e) {
            throw new RuntimeException("Error al procesar el archivo en el almacenamiento de Azure", e);
        }

        UserFile userFile = new UserFile();
        userFile.setUuid(fileUuid);
        userFile.setName(name);
        userFile.setFileType(type);
        userFile.setMimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        userFile.setCustomName(customName);
        userFile.setUploadDate(ZonedDateTime.now());
        return userFile;
    }

    public void deleteStoredFile(UserFile file) {
        String blobName = file.getUuid() + file.getName().substring(file.getName().lastIndexOf("."));
        blobStorageService.delete(blobName, getFileTypeDirectory(file.getFileType()));
    }

    private BlobDirectory getFileTypeDirectory(UserFileType fileType) {
        return switch (fileType) {
            case PROFILE -> BlobDirectory.PROFILE_PHOTOS;
            case USER_EDUCATION_STAGE -> BlobDirectory.EDUCATION_DOCUMENTS;
            case USER_ACTIVITIES -> BlobDirectory.ACTIVITY_ATTACHMENTS;
            case CHAT_PICTURE -> BlobDirectory.CHAT_PHOTOS;
        };
    }
}
