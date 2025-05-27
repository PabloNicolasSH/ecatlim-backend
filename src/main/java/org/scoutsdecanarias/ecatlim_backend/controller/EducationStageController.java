package org.scoutsdecanarias.ecatlim_backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.entity.EducationStage;
import org.scoutsdecanarias.ecatlim_backend.service.EducationStageService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/education-stage")
public class EducationStageController {

    private final EducationStageService educationStageService;

    public EducationStageController(EducationStageService educationStageService) {
        this.educationStageService = educationStageService;
    }

    @GetMapping("/admin/all")
    public List<EducationStage> getAllEducationStages() {
        log.info("METHOD getAllEducationStages() - Get all the education stages by {}", SecurityContextHolder.getContext().getAuthentication().getName());
        return this.educationStageService.getEducationStages();
    }
}
