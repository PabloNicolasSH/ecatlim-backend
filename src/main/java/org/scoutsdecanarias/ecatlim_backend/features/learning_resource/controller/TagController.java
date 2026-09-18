package org.scoutsdecanarias.ecatlim_backend.features.learning_resource.controller;

import lombok.RequiredArgsConstructor;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.dto.TagDto;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.service.TagService;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/add")
    public ResponseEntity<TagDto> createTag(@RequestBody String tagName) {
        return ResponseEntity.ok(TagDto.fromEntity(tagService.createTag(tagName)));
    }
}
