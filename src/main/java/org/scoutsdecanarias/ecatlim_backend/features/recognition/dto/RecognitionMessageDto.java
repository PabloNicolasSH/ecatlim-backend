package org.scoutsdecanarias.ecatlim_backend.features.recognition.dto;

import org.scoutsdecanarias.ecatlim_backend.features.recognition.entity.RecognitionMessage;
import org.scoutsdecanarias.ecatlim_backend.features.recognition.enums.RecognitionMessageKind;
import org.scoutsdecanarias.ecatlim_backend.features.user_file.UserFile;

import java.time.ZonedDateTime;
import java.util.List;

public record RecognitionMessageDto(
        Integer id,
        RecognitionMessageKind kind,
        String authorName,
        String comment,
        ZonedDateTime createdAt,
        List<RecognitionFileDto> files
) {
    public record RecognitionFileDto(Integer fileId, String name, String mimeType) {
        static RecognitionFileDto fromEntity(UserFile file) {
            return new RecognitionFileDto(file.getId(), file.getName(), file.getMimeType());
        }
    }

    public static RecognitionMessageDto fromEntity(RecognitionMessage message) {
        return new RecognitionMessageDto(
                message.getId(),
                message.getKind(),
                RecognitionRequestDto.fullName(message.getAuthor()),
                message.getComment(),
                message.getCreatedAt(),
                message.getFiles().stream().map(RecognitionFileDto::fromEntity).toList()
        );
    }
}
