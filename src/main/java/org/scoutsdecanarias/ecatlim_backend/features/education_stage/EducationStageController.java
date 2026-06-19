package org.scoutsdecanarias.ecatlim_backend.features.education_stage;

import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.features.education_stage.dto.EducationStageCardDto;
import org.scoutsdecanarias.ecatlim_backend.features.education_stage.dto.EducationStageDto;
import org.scoutsdecanarias.ecatlim_backend.features.education_stage.dto.EducationStageFormDto;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
        return EducationStageDto.fromCollection(educationStageService.getEducationStages());
    }

    @PostMapping("/admin/add")
    public EducationStageDto addEducationStage(@RequestBody EducationStageFormDto educationStage) {
        log.info("METHOD addEducationStage() - Add education stage by {}, the education stage is {}",
                SecurityContextHolder.getContext().getAuthentication().getName(),
                educationStage.name());
        return EducationStageDto.fromEntity(educationStageService.createEducationStage(educationStage));
    }

    @GetMapping("/{id}")
    public EducationStageDto getEducationStageById(@PathVariable int id) {
        log.info("METHOD getEducationStageById() - Getting EducationStage with id {}", id);
        return EducationStageDto.fromEntity(educationStageService.getEducationStage(id));
    }

    @GetMapping("/offer")
    public List<EducationStageCardDto> getOfferedEducationStages() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return educationStageService.getEducationOfferForUser(userEmail);
    }
}
