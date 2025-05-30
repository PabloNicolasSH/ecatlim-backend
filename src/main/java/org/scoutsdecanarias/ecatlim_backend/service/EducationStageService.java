package org.scoutsdecanarias.ecatlim_backend.service;

import org.scoutsdecanarias.ecatlim_backend.dto.EducationStageFormDto;
import org.scoutsdecanarias.ecatlim_backend.entity.EducationStage;
import org.scoutsdecanarias.ecatlim_backend.repository.EducationStageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EducationStageService {

    private final EducationStageRepository educationStageRepository;

    public EducationStageService(EducationStageRepository educationStageRepository) {
        this.educationStageRepository = educationStageRepository;
    }

    public List<EducationStage> getEducationStages() {
        return educationStageRepository.findAll();
    }

    public EducationStage getEducationStage(int id) {
        return educationStageRepository.findEducationStageById(id);
    }

    public EducationStage createEducationStage(EducationStageFormDto educationStageFormDto) {

        EducationStage educationStage = new EducationStage();
        educationStage.setName(educationStageFormDto.name());
        educationStage.setCode(educationStageFormDto.code());
        educationStage.setDescription(educationStageFormDto.description());

        educationStage.setOnlineHours(educationStageFormDto.onlineHours());
        educationStage.setContactHours(educationStageFormDto.contactHours());
        educationStage.setPracticalHours(educationStageFormDto.practicalHours());

        educationStage.setPreviousStageRequired(educationStageFormDto.previousStageRequired());
        educationStage.setPreviousStage(this.getEducationStage(educationStageFormDto.previousStageId()));

        return educationStageRepository.save(educationStage);
    }

    public EducationStage updateEducationStage(EducationStage educationStage, Integer id) {
        EducationStage educationStageToUpdate = educationStageRepository.findEducationStageById(id);

        educationStageToUpdate.setName(educationStage.getName());
        educationStageToUpdate.setDescription(educationStage.getDescription());
        educationStageToUpdate.setCode(educationStage.getCode());
        educationStageToUpdate.setModules(educationStage.getModules());

        educationStageToUpdate.setOnlineHours(educationStage.getOnlineHours());
        educationStageToUpdate.setContactHours(educationStage.getContactHours());
        educationStageToUpdate.setPracticalHours(educationStage.getPracticalHours());

        educationStageToUpdate.setPreviousStageRequired(educationStage.isPreviousStageRequired());
        educationStageToUpdate.setPreviousStage(this.getEducationStage(educationStage.getPreviousStage().getId()));

        return educationStageRepository.save(educationStageToUpdate);
    }
}
