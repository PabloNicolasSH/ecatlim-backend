package org.scoutsdecanarias.ecatlim_backend.features.news.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.features.news.dto.NewsDto;
import org.scoutsdecanarias.ecatlim_backend.features.news.dto.NewsFormDto;
import org.scoutsdecanarias.ecatlim_backend.features.news.service.NewsService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/news")
public class NewsController {

    private static final String MANAGERS = "hasAnyAuthority('ADMIN', 'MANAGER_DIRECTOR', 'MANAGEMENT')";

    private final NewsService newsService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<NewsDto>> getLatest(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(NewsDto.fromCollection(newsService.getLatest(Math.max(page, 0), Math.min(Math.max(size, 1), 50))));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> image(@PathVariable Integer id) {
        return newsService.image(id, false);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/image/thumbnail")
    public ResponseEntity<byte[]> thumbnail(@PathVariable Integer id) {
        return newsService.image(id, true);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/attachments/{attachmentId}/download")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Integer id, @PathVariable Integer attachmentId) {
        return newsService.downloadAttachment(id, attachmentId);
    }

    @PreAuthorize(MANAGERS)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NewsDto> create(@Valid @RequestPart("data") NewsFormDto data, @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        return ResponseEntity.ok(NewsDto.fromEntity(newsService.create(data, image)));
    }

    @PreAuthorize(MANAGERS)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NewsDto> update(@PathVariable Integer id, @Valid @RequestPart("data") NewsFormDto data, @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        return ResponseEntity.ok(NewsDto.fromEntity(newsService.update(id, data, image)));
    }

    @PreAuthorize(MANAGERS)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        newsService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(MANAGERS)
    @PostMapping(value = "/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NewsDto> addAttachment(@PathVariable Integer id, @RequestPart("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(NewsDto.fromEntity(newsService.addAttachment(id, file)));
    }

    @PreAuthorize(MANAGERS)
    @DeleteMapping("/{id}/attachments/{attachmentId}")
    public ResponseEntity<NewsDto> removeAttachment(@PathVariable Integer id, @PathVariable Integer attachmentId) {
        return ResponseEntity.ok(NewsDto.fromEntity(newsService.removeAttachment(id, attachmentId)));
    }
}
