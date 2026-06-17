package org.scoutsdecanarias.ecatlim_backend.event.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record EventConfigDto(
        Integer minParticipants,
        LocalDateTime dateOpenInscription,
        LocalDateTime dateCloseInscription,
        Integer cost,
        String transferBankNumber,
        String transferCode,
        Set<String> notificationTarget
) {
}
