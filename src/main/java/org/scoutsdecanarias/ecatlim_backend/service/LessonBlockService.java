package org.scoutsdecanarias.ecatlim_backend.service;

import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.LessonBlock;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.LessonBlockRepository;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.dto.LessonBlockDto;
import org.scoutsdecanarias.ecatlim_backend.repository.ModuleRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class LessonBlockService {

    private final LessonBlockRepository lessonBlockRepository;
    private final ModuleRepository moduleRepository;

    public LessonBlockService(LessonBlockRepository lessonBlockRepository, ModuleRepository moduleRepository) {
        this.lessonBlockRepository = lessonBlockRepository;
        this.moduleRepository = moduleRepository;
    }

    public List<LessonBlock> getLessonBlocks() {
        return lessonBlockRepository.findAll();
    }

    public LessonBlock getLessonBlockById(Integer id) {
        return lessonBlockRepository.findById(id).orElseThrow(NoSuchElementException::new);
    }

    public void addLessonBlocks(List<LessonBlockDto> lessonBlocks) {
        List<LessonBlock> newLessonBlocks = new ArrayList<LessonBlock>();

        for (LessonBlockDto lessonBlock : lessonBlocks) {
            LessonBlock lessonBlockEntity = new LessonBlock();

            lessonBlockEntity.setName(lessonBlock.name());
            lessonBlockEntity.setDescription(lessonBlock.description());
            lessonBlockEntity.setOnlineHours(lessonBlock.onlineHours());
            lessonBlockEntity.setContactHours(lessonBlock.contactHours());
            lessonBlockEntity.setLessonBlockId(lessonBlock.lessonBlockId());
            lessonBlockEntity.setRecognizable(lessonBlock.recognizable());

            if (lessonBlock.moduleId() != null){
                lessonBlockEntity.setModule(this.moduleRepository.findById(lessonBlock.moduleId()).orElseThrow(NoSuchElementException::new));
            }

            newLessonBlocks.add(lessonBlockEntity);
        }

        this.lessonBlockRepository.saveAll(newLessonBlocks);
    }
}
