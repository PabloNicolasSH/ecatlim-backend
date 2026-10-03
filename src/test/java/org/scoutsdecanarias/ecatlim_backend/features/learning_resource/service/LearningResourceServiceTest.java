package org.scoutsdecanarias.ecatlim_backend.features.learning_resource.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.ResourceType;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.dto.LearningResourceUploadDto;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.entity.LearningResource;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.repository.LearningResourceRepository;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.repository.TagRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.scoutsdecanarias.ecatlim_backend.shared.blob.BlobDirectory;
import org.scoutsdecanarias.ecatlim_backend.shared.blob.BlobStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningResourceServiceTest {

    @Mock LearningResourceRepository learningResourceRepository;
    @Mock TagRepository tagRepository;
    @Mock BlobStorageService blobStorageService;
    @Mock UserRepository userRepository;

    @InjectMocks LearningResourceService learningResourceService;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void loginAs(String email, String... roles) {
        var authorities = java.util.Arrays.stream(roles).map(SimpleGrantedAuthority::new).toList();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(email, null, authorities));
    }

    private LearningResource resource(String authorEmail, ResourceType type) {
        User author = new User();
        author.setEmail(authorEmail);
        LearningResource resource = new LearningResource();
        resource.setId(1);
        resource.setUser(author);
        resource.setResourceType(type);
        resource.setBlobPath("abc.pdf");
        resource.setMimeType("application/pdf");
        return resource;
    }

    @Test
    void onlyAuthorOrManagersCanManage() {
        LearningResource resource = resource("author@test.com", ResourceType.PDF);

        loginAs("author@test.com", "TRAINER");
        assertThat(learningResourceService.canManage(resource)).isTrue();

        loginAs("other@test.com", "TRAINER");
        assertThat(learningResourceService.canManage(resource)).isFalse();

        loginAs("admin@test.com", "ADMIN");
        assertThat(learningResourceService.canManage(resource)).isTrue();
    }

    @Test
    void otherTrainerCannotDeleteResource() {
        loginAs("other@test.com", "TRAINER");
        when(learningResourceRepository.findById(1)).thenReturn(Optional.of(resource("author@test.com", ResourceType.PDF)));

        assertThatThrownBy(() -> learningResourceService.delete(1))
                .isInstanceOf(EcatlimException.class)
                .extracting("status").isEqualTo(HttpStatus.FORBIDDEN);
        verify(learningResourceRepository, never()).delete(any());
        verify(blobStorageService, never()).delete(any(), any());
    }

    @Test
    void deletingFileResourceRemovesItsBlob() {
        loginAs("author@test.com", "TRAINER");
        when(learningResourceRepository.findById(1)).thenReturn(Optional.of(resource("author@test.com", ResourceType.PDF)));

        learningResourceService.delete(1);

        verify(blobStorageService).delete("abc.pdf", BlobDirectory.RESOURCES);
    }

    @Test
    void fileMustMatchResourceType() {
        loginAs("author@test.com", "TRAINER");
        MockMultipartFile notAPdf = new MockMultipartFile("file", "photo.png", "image/png", new byte[]{1});
        LearningResourceUploadDto dto = new LearningResourceUploadDto("Guía", null, null, ResourceType.PDF, List.of());

        assertThatThrownBy(() -> learningResourceService.create(dto, notAPdf))
                .isInstanceOf(EcatlimException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(blobStorageService);
    }
}
