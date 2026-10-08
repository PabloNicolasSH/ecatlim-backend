package org.scoutsdecanarias.ecatlim_backend.features.module;

import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.scoutsdecanarias.ecatlim_backend.core.exception.ResourceNotFoundException;
import org.scoutsdecanarias.ecatlim_backend.features.education_stage.EducationStage;
import org.scoutsdecanarias.ecatlim_backend.features.education_stage.EducationStageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            moduleEntity.setModuleId(module.moduleId());
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

    @Transactional
    public void updateModule(Integer id, ModuleDto form) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo no encontrado"));

        ModuleType type = Objects.equals(form.type(), "Teórico") || Objects.equals(form.type(), "THEORETICAL")
                ? ModuleType.THEORETICAL
                : ModuleType.PRACTICAL;

        validateStageHours(module, type, form.onlineHours(), form.contactHours());

        module.setName(form.name());
        module.setModuleId(form.moduleId());
        module.setDescription(form.description());
        module.setType(type);
        module.setOnlineHours(form.onlineHours());
        module.setContactHours(form.contactHours());

        moduleRepository.save(module);
    }

    private void validateStageHours(Module module, ModuleType type, int onlineHours, int contactHours) {
        EducationStage stage = module.getEducationStage();
        if (stage == null) return;

        int[] before = stageHours(stage, null, null, 0, 0);
        int[] after = stageHours(stage, module, type, onlineHours, contactHours);
        int[] limits = {stage.getOnlineHours(), stage.getContactHours(), stage.getPracticalHours()};

        for (int i = 0; i < limits.length; i++) {
            if (after[i] > limits[i] && after[i] > before[i]) {
                throw new EcatlimException("Las horas de los módulos superan las horas totales de la etapa formativa", HttpStatus.BAD_REQUEST);
            }
        }
    }

    private int[] stageHours(EducationStage stage, Module edited, ModuleType editedType, int editedOnline, int editedContact) {
        int online = 0;
        int contact = 0;
        int practical = 0;
        for (Module other : stage.getModules()) {
            boolean isEdited = edited != null && other.getId().equals(edited.getId());
            ModuleType otherType = isEdited ? editedType : other.getType();
            int otherOnline = isEdited ? editedOnline : other.getOnlineHours();
            int otherContact = isEdited ? editedContact : other.getContactHours();

            if (otherType == ModuleType.THEORETICAL) {
                online += otherOnline;
                contact += otherContact;
            } else {
                practical += otherOnline + otherContact;
            }
        }
        return new int[]{online, contact, practical};
    }
}
