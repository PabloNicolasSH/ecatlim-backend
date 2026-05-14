package org.scoutsdecanarias.ecatlim_backend.service;

import org.scoutsdecanarias.ecatlim_backend.dto.education_stage.EducationStageCardDto;
import org.scoutsdecanarias.ecatlim_backend.dto.education_stage.EducationStageFormDto;
import org.scoutsdecanarias.ecatlim_backend.entity.EducationStage;
import org.scoutsdecanarias.ecatlim_backend.entity.UserEducationStage;
import org.scoutsdecanarias.ecatlim_backend.repository.EducationStageRepository;
import org.scoutsdecanarias.ecatlim_backend.repository.UserEducationStageRepository;
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
