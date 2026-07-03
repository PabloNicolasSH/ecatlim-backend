package org.scoutsdecanarias.ecatlim_backend.features.learning_resource.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.scoutsdecanarias.ecatlim_backend.core.exception.ResourceNotFoundException;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.ResourceType;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.dto.LearningResourceUploadDto;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.entity.LearningResource;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.entity.Tag;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.repository.LearningResourceRepository;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.repository.TagRepository;
import org.scoutsdecanarias.ecatlim_backend.shared.blob.BlobDirectory;
import org.scoutsdecanarias.ecatlim_backend.shared.blob.BlobStorageService;
import org.scoutsdecanarias.ecatlim_backend.shared.utils.FileTransferDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class LearningResourceService {

    private final LearningResourceRepository learningResourceRepository;
    private final TagRepository tagRepository;
    private final BlobStorageService blobStorageService;

    public List<LearningResource> getAll() {
        return learningResourceRepository.findAll();
    }

    public LearningResource create(LearningResourceUploadDto dto, MultipartFile file) throws IOException {
        String finalPath;
        String mimeType;

        boolean isLinkOrVideo = dto.type() == ResourceType.LINK || dto.type() == ResourceType.VIDEO_LINK;

        if (isLinkOrVideo) {
            finalPath = dto.blobPath();
            mimeType = "text/uri-list";
        } else {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("El archivo es obligatorio para este tipo de recurso.");
            }
            var response = blobStorageService.upload(file, BlobDirectory.RESOURCES, file.getOriginalFilename());
            finalPath = response.fileName();
            mimeType = file.getContentType();
        }

        Set<Tag> tags = new HashSet<>(tagRepository.findByNameIn(dto.tagNames()));

        LearningResource resource = new LearningResource();
        resource.setName(dto.name());
        resource.setDescription(dto.description());
        resource.setBlobPath(finalPath);
        resource.setMimeType(mimeType);
        resource.setResourceType(dto.type());
        resource.setTags(tags);

        return learningResourceRepository.save(resource);
    }

    public ResponseEntity<byte[]> downloadResource(Integer id) {
        LearningResource resource = learningResourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso no encontrado"));
        byte[] fileBytes = blobStorageService.download(resource.getBlobPath(), BlobDirectory.RESOURCES);
        return new FileTransferDto(fileBytes, resource.getName(), resource.getMimeType()).asResponseEntity();
    }
}
