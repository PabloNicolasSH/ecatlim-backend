package org.scoutsdecanarias.ecatlim_backend.service;

import org.scoutsdecanarias.ecatlim_backend.entity.LessonBlock;
import org.scoutsdecanarias.ecatlim_backend.repository.LessonBlockRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class LessonBlockService {

    private final LessonBlockRepository lessonBlockRepository;

    public LessonBlockService(LessonBlockRepository lessonBlockRepository) {
        this.lessonBlockRepository = lessonBlockRepository;
    }

    public List<LessonBlock> getLessonBlocks() {
        return lessonBlockRepository.findAll();
    }

    public LessonBlock getLessonBlockById(Integer id) {
        return lessonBlockRepository.findById(id).orElseThrow(NoSuchElementException::new);
    }
}
