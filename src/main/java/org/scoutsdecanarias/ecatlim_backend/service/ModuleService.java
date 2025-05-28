package org.scoutsdecanarias.ecatlim_backend.service;

import org.scoutsdecanarias.ecatlim_backend.dto.ModuleDto;
import org.scoutsdecanarias.ecatlim_backend.entity.Module;
import org.scoutsdecanarias.ecatlim_backend.enums.ModuleType;
import org.scoutsdecanarias.ecatlim_backend.repository.EducationStageRepository;
import org.scoutsdecanarias.ecatlim_backend.repository.ModuleRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final EducationStageRepository educationStageRepository;

    public ModuleService(ModuleRepository moduleRepository, EducationStageRepository educationStageRepository) {
        this.moduleRepository = moduleRepository;
        this.educationStageRepository = educationStageRepository;
    }

    public List<Module> getAll() {
        return moduleRepository.findAll();
    }

    public void addModules(List<ModuleDto> modules) {
        List<Module> modulesList = new ArrayList<Module>();

        for (ModuleDto module : modules) {
            Module moduleEntity = new Module();
            moduleEntity.setName(module.name());
            moduleEntity.setDescription(module.description());
            moduleEntity.setContactHours(module.contactHours());
            moduleEntity.setOnlineHours(module.onlineHours());
            moduleEntity.setEducationStage(this.educationStageRepository.findEducationStageById(module.educationStage()));

            if (Objects.equals(module.type(), "Teórico")){
                moduleEntity.setType(ModuleType.THEORETICAL);
            } else {
                moduleEntity.setType(ModuleType.PRACTICAL);
            }

            modulesList.add(moduleEntity);
        }

        this.moduleRepository.saveAll(modulesList);
    }
}
