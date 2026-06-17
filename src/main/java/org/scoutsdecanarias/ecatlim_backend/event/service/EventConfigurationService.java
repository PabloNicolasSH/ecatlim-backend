package org.scoutsdecanarias.ecatlim_backend.event.service;

import lombok.AllArgsConstructor;
import org.scoutsdecanarias.ecatlim_backend.event.dto.EventConfigDto;
import org.scoutsdecanarias.ecatlim_backend.event.entity.EventConfiguration;
import org.scoutsdecanarias.ecatlim_backend.event.enums.NotificationTarget;
import org.scoutsdecanarias.ecatlim_backend.event.repository.EventConfigurationRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EventConfigurationService {

    private final EventConfigurationRepository eventConfigurationRepository;

    public EventConfiguration add(EventConfigDto eventConfigDto) {
        EventConfiguration eventConfiguration = new EventConfiguration();

        eventConfiguration.setMinParticipants(eventConfigDto.minParticipants());
        eventConfiguration.setCost(eventConfigDto.cost());
        eventConfiguration.setTransferCode(eventConfigDto.transferCode());
        eventConfiguration.setTransferBankNumber(eventConfigDto.transferBankNumber());
        eventConfiguration.setDateOpenInscription(eventConfigDto.dateOpenInscription());
        eventConfiguration.setDateCloseInscription(eventConfigDto.dateCloseInscription());

        if (eventConfigDto.notificationTarget() != null) {
            Set<NotificationTarget> notificationTargets = eventConfigDto.notificationTarget().stream()
                    .map(targetStr -> NotificationTarget.valueOf(targetStr.trim()))
                    .collect(Collectors.toSet());
            eventConfiguration.setNotificationTarget(notificationTargets);
        }

        return eventConfigurationRepository.save(eventConfiguration);
    }

    public EventConfiguration update(Integer id, EventConfiguration eventConfiguration) {
        EventConfiguration eventConfigToUpdate = eventConfigurationRepository.getReferenceById(id);

        eventConfigToUpdate.setMinParticipants(eventConfiguration.getMinParticipants());
        eventConfigToUpdate.setCost(eventConfiguration.getCost());
        eventConfigToUpdate.setTransferCode(eventConfiguration.getTransferCode());
        eventConfigToUpdate.setTransferBankNumber(eventConfiguration.getTransferBankNumber());
        eventConfigToUpdate.setDateOpenInscription(eventConfiguration.getDateOpenInscription());
        eventConfigToUpdate.setDateCloseInscription(eventConfiguration.getDateCloseInscription());
        eventConfigToUpdate.setNotificationTarget(eventConfiguration.getNotificationTarget());

        return eventConfigurationRepository.save(eventConfigToUpdate);
    }
}
