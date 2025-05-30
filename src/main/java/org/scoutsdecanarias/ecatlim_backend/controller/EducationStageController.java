package org.scoutsdecanarias.ecatlim_backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.dto.EducationStageDto;
import org.scoutsdecanarias.ecatlim_backend.dto.EducationStageFormDto;
import org.scoutsdecanarias.ecatlim_backend.entity.EducationStage;
import org.scoutsdecanarias.ecatlim_backend.service.EducationStageService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public List<EducationStageDto> getAllEducationStages() {
        log.info("METHOD getAllEducationStages() - Get all the education stages by {}",
                SecurityContextHolder.getContext().getAuthentication().getName());
        return EducationStageDto.fromCollection(this.educationStageService.getEducationStages());
    }

    @GetMapping("/{id}")
    public EducationStageDto getEducationStageById(@PathVariable int id) {
        log.info("METHOD getEducationStageById() - Getting EducationStage with id {}", id);
        return EducationStageDto.fromEntity(this.educationStageService.getEducationStage(id));
    }

    @PostMapping("/admin/add")
    public EducationStageDto addEducationStage(@RequestBody EducationStageFormDto educationStage) {
        log.info("METHOD addEducationStage() - Add education stage by {}, the education stage is {}",
                SecurityContextHolder.getContext().getAuthentication().getName(),
                educationStage.name());
        return EducationStageDto.fromEntity(this.educationStageService.createEducationStage(educationStage));
    }
}
