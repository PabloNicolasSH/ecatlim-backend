package org.scoutsdecanarias.ecatlim_backend.features.event.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import org.scoutsdecanarias.ecatlim_backend.features.event.entity.EventConfiguration;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

public record EventConfigDto(
        @NotNull(message = "El mínimo de participantes es obligatorio")
        @Min(value = 1, message = "El mínimo de participantes debe ser al menos 1")
        Integer minParticipants,

        @NotNull(message = "La fecha de apertura de inscripción es obligatoria")
        LocalDateTime dateOpenInscription,

        @NotNull(message = "La fecha de cierre de inscripción es obligatoria")
        LocalDateTime dateCloseInscription,

        @NotNull(message = "El coste es obligatorio")
        @Min(value = 0, message = "El coste no puede ser negativo")
        Integer cost,

        @NotBlank(message = "El número de cuenta para la transferencia es obligatorio")
        @Size(max = 255, message = "El número de cuenta no puede superar los 255 caracteres")
        String transferBankNumber,

        @NotBlank(message = "El concepto de la transferencia es obligatorio")
        @Size(max = 255, message = "El concepto de la transferencia no puede superar los 255 caracteres")
        String transferCode,

        @NotEmpty(message = "Debes seleccionar a quién notificar")
        Set<@NotNull @Pattern(regexp = "INTERESTED_USERS|AVAILABLE_USERS|HEAD_OF_EDUCATION",
                message = "El destinatario de la notificación no es válido") String> notificationTarget
) {
    @JsonIgnore
    @AssertTrue(message = "El cierre de inscripción debe ser posterior a su apertura")
    public boolean isInscriptionCloseAfterOpen() {
        return dateOpenInscription == null || dateCloseInscription == null || dateCloseInscription.isAfter(dateOpenInscription);
    }

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
