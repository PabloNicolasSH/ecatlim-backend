package org.scoutsdecanarias.ecatlim_backend.features.recognition.dto;

import org.scoutsdecanarias.ecatlim_backend.features.recognition.entity.RecognitionRequest;
import org.scoutsdecanarias.ecatlim_backend.features.recognition.enums.RecognitionStatus;
import org.scoutsdecanarias.ecatlim_backend.features.recognition.enums.RecognitionType;

/** Light view of the latest request, embedded in each block of the student's progress. */
public record RecognitionSummaryDto(Integer id, RecognitionType type, RecognitionStatus status) {
    public static RecognitionSummaryDto fromEntity(RecognitionRequest request) {
        return request == null ? null : new RecognitionSummaryDto(request.getId(), request.getType(), request.getStatus());
    }
}
