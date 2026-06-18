package org.scoutsdecanarias.ecatlim_backend.event.dto;

import org.scoutsdecanarias.ecatlim_backend.event.entity.EventConfiguration;

import java.time.LocalDateTime;
import java.util.Collections;
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
    public static EventConfigDto fromEntity(EventConfiguration eventConfiguration) {
        return new EventConfigDto(
                eventConfiguration.getMinParticipants(),
                eventConfiguration.getDateOpenInscription(),
                eventConfiguration.getDateCloseInscription(),
                eventConfiguration.getCost(),
                eventConfiguration.getTransferBankNumber(),
                eventConfiguration.getTransferCode(),
                Collections.singleton(eventConfiguration.getNotificationTarget().toString())
        );
    }
}
