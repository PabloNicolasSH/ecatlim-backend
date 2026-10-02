package org.scoutsdecanarias.ecatlim_backend.features.education_stage;

import jakarta.transaction.Transactional;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.scoutsdecanarias.ecatlim_backend.core.exception.ResourceNotFoundException;
import org.scoutsdecanarias.ecatlim_backend.features.education_stage.dto.EducationStageCardDto;
import org.scoutsdecanarias.ecatlim_backend.features.education_stage.dto.EducationStageDto;
import org.scoutsdecanarias.ecatlim_backend.features.education_stage.dto.EducationStageFormDto;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserEducationStageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EducationStageService {

    private final EducationStageRepository educationStageRepository;
    private final UserEducationStageRepository userEducationStageRepository;

    public EducationStageService(EducationStageRepository educationStageRepository, UserEducationStageRepository userEducationStageRepository) {
        this.educationStageRepository = educationStageRepository;
        this.userEducationStageRepository = userEducationStageRepository;
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

    @Transactional
    public EducationStage updateEducationStage(Integer id, EducationStageFormDto form) {
        EducationStage stage = educationStageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etapa formativa no encontrada"));

        EducationStageDto current = EducationStageDto.fromEntity(stage);
        if (form.onlineHours() < current.allocatedOnlineHours()
                || form.contactHours() < current.allocatedContactHours()
                || form.practicalHours() < current.allocatedPracticalHours()) {
            throw new EcatlimException("Las horas totales no pueden ser inferiores a las ya asignadas a los módulos", HttpStatus.BAD_REQUEST);
        }

        EducationStage previousStage = null;
        if (Boolean.TRUE.equals(form.previousStageRequired())) {
            previousStage = educationStageRepository.findById(form.previousStageId())
                    .orElseThrow(() -> new EcatlimException("La etapa previa indicada no existe", HttpStatus.BAD_REQUEST));
            if (previousStage.getId().equals(id)) {
                throw new EcatlimException("Una etapa no puede ser su propia etapa previa", HttpStatus.BAD_REQUEST);
            }
        }

        stage.setName(form.name());
        stage.setCode(form.code());
        stage.setDescription(form.description());
        stage.setOnlineHours(form.onlineHours());
        stage.setContactHours(form.contactHours());
        stage.setPracticalHours(form.practicalHours());
        stage.setPreviousStageRequired(previousStage != null);
        stage.setPreviousStage(previousStage);

        return educationStageRepository.save(stage);
    }

    public List<EducationStageCardDto> getEducationOfferForUser(String userEmail) {
        List<EducationStage> educationStages = educationStageRepository.findAll();
        List<UserEducationStage> userEnrollments = userEducationStageRepository.findByUser_Email(userEmail);

        return educationStages.stream().map(stage -> {
            Optional<UserEducationStage> enrollment = userEnrollments.stream()
                    .filter(ue -> ue.getEducationStage().getId().equals(stage.getId()))
                    .findFirst();

            boolean isEnabled;

            if (stage.getPreviousStage() == null) {
                isEnabled = true;
            } else {
                Integer previousId = stage.getPreviousStage().getId();

                isEnabled = userEnrollments.stream()
                        .anyMatch(ue -> ue.getEducationStage().getId().equals(previousId)
                                && "COMPLETED".equals(ue.getStatus().toString()));
            }

            String status = enrollment.map(ue -> ue.getStatus().toString()).orElse(isEnabled ? "AVAILABLE" : "LOCKED");

            if (enrollment.isPresent()) isEnabled = true;

            return new EducationStageCardDto(
                    stage.getId(),
                    stage.getName(),
                    stage.getDescription(),
                    stage.getCode(),
                    status,
                    isEnabled
            );
        }).collect(Collectors.toList());
    }
}
