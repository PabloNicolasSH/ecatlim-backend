package org.scoutsdecanarias.ecatlim_backend.features.event.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.scoutsdecanarias.ecatlim_backend.features.event.entity.EventContact;

public record EventContactDto(
        Integer id,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 255, message = "El nombre no puede superar los 255 caracteres")
        String name,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 255, message = "Los apellidos no pueden superar los 255 caracteres")
        String surname,

        @NotBlank(message = "El móvil es obligatorio")
        @Size(max = 255, message = "El móvil no puede superar los 255 caracteres")
        String phone,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es válido")
        @Size(max = 255, message = "El correo no puede superar los 255 caracteres")
        String email,

        Integer originEntityId,

        String originEntityName,

        @Size(max = 255, message = "El ámbito o profesión no puede superar los 255 caracteres")
        String specialty
) {
    public static EventContactDto fromEntity(EventContact contact) {
        return new EventContactDto(contact.getId(), contact.getName(), contact.getSurname(),
                contact.getPhone(), contact.getEmail(),
                contact.getOriginEntity() != null ? contact.getOriginEntity().getId() : null,
                contact.getOriginEntity() != null ? contact.getOriginEntity().getName() : null,
                contact.getSpecialty());
    }
}
