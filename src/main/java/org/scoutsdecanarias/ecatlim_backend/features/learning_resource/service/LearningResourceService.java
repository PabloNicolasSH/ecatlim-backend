package org.scoutsdecanarias.ecatlim_backend.features.learning_resource.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.scoutsdecanarias.ecatlim_backend.core.auth.SecurityUtils;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.scoutsdecanarias.ecatlim_backend.core.exception.ResourceNotFoundException;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.ResourceType;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.dto.LearningResourceUploadDto;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.entity.LearningResource;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.entity.Tag;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.repository.LearningResourceRepository;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.repository.TagRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.enums.Role;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.scoutsdecanarias.ecatlim_backend.shared.blob.BlobDirectory;
import org.scoutsdecanarias.ecatlim_backend.shared.blob.BlobStorageService;
import org.scoutsdecanarias.ecatlim_backend.shared.utils.FileNames;
import org.scoutsdecanarias.ecatlim_backend.shared.utils.FileTransferDto;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LearningResourceService {

    private static final String LINK_MIME_TYPE = "text/uri-list";
    private static final int THUMBNAIL_SIZE = 480;
    private static final Set<String> MANAGER_AUTHORITIES = Set.of(Role.ADMIN.name(), Role.MANAGEMENT.name());

    private final LearningResourceRepository learningResourceRepository;
    private final TagRepository tagRepository;
    private final BlobStorageService blobStorageService;
    private final UserRepository userRepository;

    public List<LearningResource> getAll() {
        return learningResourceRepository.findAllByOrderByIdDesc();
    }

    public LearningResource create(LearningResourceUploadDto dto, MultipartFile file) throws IOException {
        LearningResource resource = new LearningResource();
        resource.setUser(userRepository.findByEmail(SecurityUtils.getLoggedUsername()).orElse(null));
        resource.setCreatedAt(LocalDateTime.now());

        if (isLink(dto.type())) {
            resource.setBlobPath(dto.blobPath().trim());
            resource.setMimeType(LINK_MIME_TYPE);
        } else {
            if (file == null || file.isEmpty()) {
                throw new EcatlimException("El archivo es obligatorio para este tipo de recurso", HttpStatus.BAD_REQUEST);
            }
            storeFile(resource, dto.type(), file);
        }

        applyMetadata(resource, dto);
        log.info("METHOD create() - Learning resource created {}", resource.getName());
        return learningResourceRepository.save(resource);
    }

    public LearningResource update(Integer id, LearningResourceUploadDto dto, MultipartFile file) throws IOException {
        LearningResource resource = findById(id);
        checkCanManage(resource);

        String previousFile = isLink(resource.getResourceType()) ? null : resource.getBlobPath();
        boolean newFile = file != null && !file.isEmpty();

        if (isLink(dto.type())) {
            resource.setBlobPath(dto.blobPath().trim());
            resource.setMimeType(LINK_MIME_TYPE);
        } else if (newFile) {
            storeFile(resource, dto.type(), file);
        } else if (previousFile == null) {
            throw new EcatlimException("Sube el archivo del recurso para cambiar un enlace a este tipo", HttpStatus.BAD_REQUEST);
        } else {
            checkFileMatchesType(dto.type(), resource.getMimeType(), resource.getBlobPath());
        }

        applyMetadata(resource, dto);
        LearningResource saved = learningResourceRepository.saveAndFlush(resource);

        if (previousFile != null && !previousFile.equals(saved.getBlobPath())) {
            blobStorageService.delete(previousFile, BlobDirectory.RESOURCES);
        }

        log.info("METHOD update() - Learning resource updated {}", id);
        return saved;
    }

    public void delete(Integer id) {
        LearningResource resource = findById(id);
        checkCanManage(resource);

        learningResourceRepository.delete(resource);
        learningResourceRepository.flush();
        if (!isLink(resource.getResourceType())) {
            blobStorageService.delete(resource.getBlobPath(), BlobDirectory.RESOURCES);
        }
        log.info("METHOD delete() - Learning resource deleted {}", id);
    }

    public ResponseEntity<byte[]> downloadResource(Integer id) {
        LearningResource resource = findFileResource(id);
        byte[] fileBytes = blobStorageService.download(resource.getBlobPath(), BlobDirectory.RESOURCES);
        learningResourceRepository.incrementDownloadCount(id);
        return new FileTransferDto(fileBytes, downloadFileName(resource), resource.getMimeType()).asResponseEntity();
    }

    public void registerOpen(Integer id) {
        LearningResource resource = findById(id);
        if (!isLink(resource.getResourceType())) {
            throw new EcatlimException("Solo se registran aperturas de enlaces y vídeos", HttpStatus.BAD_REQUEST);
        }
        learningResourceRepository.incrementDownloadCount(id);
    }

    public ResponseEntity<byte[]> thumbnail(Integer id) {
        LearningResource resource = findFileResource(id);
        if (resource.getResourceType() != ResourceType.IMAGE) {
            throw new EcatlimException("Este recurso no tiene miniatura", HttpStatus.BAD_REQUEST);
        }

        byte[] original = blobStorageService.download(resource.getBlobPath(), BlobDirectory.RESOURCES);
        CacheControl cache = CacheControl.maxAge(1, TimeUnit.DAYS).cachePrivate();

        String format = resource.getMimeType().toLowerCase(Locale.ROOT).contains("png") ? "png" : "jpg";
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            Thumbnails.of(new ByteArrayInputStream(original))
                    .size(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
                    .outputFormat(format)
                    .toOutputStream(os);
            MediaType mediaType = format.equals("png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
            return ResponseEntity.ok().cacheControl(cache).contentType(mediaType).body(os.toByteArray());
        } catch (IOException | RuntimeException e) {
            log.warn("METHOD thumbnail() - Could not create thumbnail for resource {}, sending the original", id);
            return ResponseEntity.ok().cacheControl(cache).contentType(MediaType.parseMediaType(resource.getMimeType())).body(original);
        }
    }

    public boolean canManage(LearningResource resource) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        boolean isManager = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(MANAGER_AUTHORITIES::contains);
        User author = resource.getUser();
        return isManager || (author != null && author.getEmail().equalsIgnoreCase(authentication.getName()));
    }

    private void checkCanManage(LearningResource resource) {
        if (!canManage(resource)) {
            throw new EcatlimException("Solo quien subió el recurso o la administración pueden modificarlo", HttpStatus.FORBIDDEN);
        }
    }

    private LearningResource findById(Integer id) {
        return learningResourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado"));
    }

    private LearningResource findFileResource(Integer id) {
        LearningResource resource = findById(id);
        if (isLink(resource.getResourceType())) {
            throw new EcatlimException("Este recurso es un enlace y no tiene archivo", HttpStatus.BAD_REQUEST);
        }
        return resource;
    }

    private void storeFile(LearningResource resource, ResourceType type, MultipartFile file) throws IOException {
        checkFileMatchesType(type, file.getContentType(), file.getOriginalFilename());
        var response = blobStorageService.upload(file, BlobDirectory.RESOURCES, FileNames.randomBlobName(file.getOriginalFilename()));
        resource.setBlobPath(response.fileName());
        resource.setMimeType(file.getContentType() != null ? file.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE);
    }

    private void checkFileMatchesType(ResourceType type, String mimeType, String fileName) {
        String mime = mimeType == null ? "" : mimeType.toLowerCase(Locale.ROOT);
        String extension = FileNames.safeExtension(fileName);
        if (type == ResourceType.PDF && !mime.equals("application/pdf") && !extension.equals(".pdf")) {
            throw new EcatlimException("El archivo debe ser un PDF", HttpStatus.BAD_REQUEST);
        }
        if (type == ResourceType.IMAGE && !mime.startsWith("image/")) {
            throw new EcatlimException("El archivo debe ser una imagen", HttpStatus.BAD_REQUEST);
        }
    }

    private void applyMetadata(LearningResource resource, LearningResourceUploadDto dto) {
        resource.setName(dto.name().trim());
        resource.setDescription(dto.description() == null || dto.description().isBlank() ? null : dto.description().trim());
        resource.setResourceType(dto.type());
        List<String> tagNames = dto.tagNames() == null ? List.of() : dto.tagNames();
        resource.setTags(new HashSet<>(tagRepository.findByNameIn(tagNames)));
    }

    private boolean isLink(ResourceType type) {
        return type == ResourceType.LINK || type == ResourceType.VIDEO_LINK;
    }

    private String downloadFileName(LearningResource resource) {
        String extension = FileNames.safeExtension(resource.getBlobPath());
        String name = resource.getName() == null || resource.getName().isBlank() ? "recurso" : resource.getName();
        return name.toLowerCase(Locale.ROOT).endsWith(extension) ? name : name + extension;
    }
}
