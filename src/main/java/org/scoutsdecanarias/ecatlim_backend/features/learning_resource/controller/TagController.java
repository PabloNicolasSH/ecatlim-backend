package org.scoutsdecanarias.ecatlim_backend.features.learning_resource.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.dto.TagDto;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.service.TagService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tags")
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<List<TagDto>> getAllTags() {
        return ResponseEntity.ok(TagDto.fromCollection(tagService.getAllTags()));
    }

    @PreAuthorize("hasAnyAuthority('MANAGEMENT', 'EVENT_DIRECTOR', 'TRAINER', 'ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<TagDto> createTag(@RequestBody @NotBlank(message = "El nombre de la etiqueta es obligatorio") @Size(max = 255, message = "El nombre de la etiqueta no puede superar los 255 caracteres") String tagName) {
        return ResponseEntity.ok(TagDto.fromEntity(tagService.createTag(tagName)));
    }
}
