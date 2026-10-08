package org.scoutsdecanarias.ecatlim_backend.features.lesson_block;

import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.scoutsdecanarias.ecatlim_backend.core.exception.ResourceNotFoundException;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.dto.LessonBlockDto;
import org.scoutsdecanarias.ecatlim_backend.features.module.Module;
import org.scoutsdecanarias.ecatlim_backend.features.module.ModuleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void updateLessonBlock(Integer id, LessonBlockDto form) {
        LessonBlock lessonBlock = lessonBlockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bloque formativo no encontrado"));

        Module module = lessonBlock.getModule();
        if (module != null) {
            int onlineBefore = 0;
            int contactBefore = 0;
            int onlineAfter = 0;
            int contactAfter = 0;
            for (LessonBlock other : module.getLessonBlocks()) {
                boolean isCurrent = other.getId().equals(lessonBlock.getId());
                onlineBefore += other.getOnlineHours();
                contactBefore += other.getContactHours();
                onlineAfter += isCurrent ? form.onlineHours() : other.getOnlineHours();
                contactAfter += isCurrent ? form.contactHours() : other.getContactHours();
            }
            if ((onlineAfter > module.getOnlineHours() && onlineAfter > onlineBefore)
                    || (contactAfter > module.getContactHours() && contactAfter > contactBefore)) {
                throw new EcatlimException("Las horas de los bloques formativos superan las horas totales del módulo", HttpStatus.BAD_REQUEST);
            }
        }

        lessonBlock.setName(form.name());
        lessonBlock.setDescription(form.description());
        lessonBlock.setLessonBlockId(form.lessonBlockId());
        lessonBlock.setOnlineHours(form.onlineHours());
        lessonBlock.setContactHours(form.contactHours());
        lessonBlock.setRecognizable(form.recognizable());

        lessonBlockRepository.save(lessonBlock);
    }
}
