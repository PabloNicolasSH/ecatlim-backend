package org.scoutsdecanarias.ecatlim_backend.features.lesson_block;

import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.dto.LessonBlockDto;
import org.scoutsdecanarias.ecatlim_backend.service.LessonBlockService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("lesson-block")
public class LessonBlockController {

    private final LessonBlockService lessonBlockService;

    public LessonBlockController(LessonBlockService lessonBlockService) {
        this.lessonBlockService = lessonBlockService;
    }

    @GetMapping("/all")
    public List<LessonBlockDto> getAllLessonBlocks() {
        return LessonBlockDto.fromCollections(lessonBlockService.getLessonBlocks());
    }

    @PostMapping("/admin/add")
    public void createLessonBlocks(@RequestBody List<LessonBlockDto> lessonBlocks) {
        log.info("METHOD createLessonBlocks() - Creating lesson blocks by {}", SecurityContextHolder.getContext().getAuthentication().getName());
        lessonBlockService.addLessonBlocks(lessonBlocks);
    }
}
