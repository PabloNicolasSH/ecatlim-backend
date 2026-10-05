package org.scoutsdecanarias.ecatlim_backend.features.recognition.enums;

public enum RecognitionStatus {
    /** Waiting for the training team to review what the student sent. */
    PENDING_REVIEW,
    /** The team asked for more documentation; waiting for the student. */
    AWAITING_DOCUMENTATION,
    APPROVED,
    REJECTED;

    public boolean isActive() {
        return this == PENDING_REVIEW || this == AWAITING_DOCUMENTATION;
    }
}
