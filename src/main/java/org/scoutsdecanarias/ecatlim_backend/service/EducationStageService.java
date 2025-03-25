package org.scoutsdecanarias.ecatlim_backend.service;

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

    public EducationStage createEducationStage(EducationStage educationStage) {
        return educationStageRepository.save(educationStage);
    }

    public EducationStage updateEducationStage(EducationStage educationStage, Integer id) {
        EducationStage educationStageToUpdate = educationStageRepository.findEducationStageById(id);

        educationStageToUpdate.setName(educationStage.getName());
        educationStageToUpdate.setDescription(educationStage.getDescription());
        educationStageToUpdate.setCode(educationStage.getCode());
        educationStageToUpdate.setModules(educationStage.getModules());

        return educationStageRepository.save(educationStageToUpdate);
    }
}
