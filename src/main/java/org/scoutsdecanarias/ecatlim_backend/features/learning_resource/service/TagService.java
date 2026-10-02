package org.scoutsdecanarias.ecatlim_backend.features.learning_resource.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.dto.TagDto;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.entity.Tag;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.repository.TagRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    @Transactional
    public Tag createTag(String name) {
        String sanitizedName = name.trim();

        if (tagRepository.existsByNameIgnoreCase(sanitizedName)) {
            throw new IllegalArgumentException("La etiqueta '" + sanitizedName + "' ya existe.");
        }

        Tag tag = new Tag();
        tag.setName(sanitizedName);

        return tagRepository.save(tag);
    }
}
