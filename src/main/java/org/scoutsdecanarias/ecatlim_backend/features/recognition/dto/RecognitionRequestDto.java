package org.scoutsdecanarias.ecatlim_backend.features.recognition.dto;

import org.scoutsdecanarias.ecatlim_backend.features.recognition.entity.RecognitionRequest;
import org.scoutsdecanarias.ecatlim_backend.features.recognition.enums.RecognitionStatus;
import org.scoutsdecanarias.ecatlim_backend.features.recognition.enums.RecognitionType;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.SimpleUserDto;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;

public record RecognitionRequestDto(
        Integer id,
        Integer lessonBlockId,
        String lessonBlockCode,
        String lessonBlockName,
        Integer userId,
        String userName,
        RecognitionType type,
        RecognitionStatus status,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt,
        List<SimpleUserDto> commission,
        List<RecognitionMessageDto> messages
) {
    /** Student view: the commission members are not exposed. */
    public static RecognitionRequestDto fromEntity(RecognitionRequest request) {
        return build(request, List.of());
    }

    public static RecognitionRequestDto fromEntityForStaff(RecognitionRequest request) {
        return build(request, request.getCommission().stream()
                .sorted(Comparator.comparing(User::getId))
                .map(SimpleUserDto::fromEntity)
                .toList());
    }

    public static List<RecognitionRequestDto> fromCollection(List<RecognitionRequest> requests) {
        return requests.stream().map(RecognitionRequestDto::fromEntity).toList();
    }

    public static List<RecognitionRequestDto> fromCollectionForStaff(List<RecognitionRequest> requests) {
        return requests.stream().map(RecognitionRequestDto::fromEntityForStaff).toList();
    }

    private static RecognitionRequestDto build(RecognitionRequest request, List<SimpleUserDto> commission) {
        return new RecognitionRequestDto(
                request.getId(),
                request.getLessonBlock().getId(),
                request.getLessonBlock().getCode(),
                request.getLessonBlock().getName(),
                request.getUser().getId(),
                fullName(request.getUser()),
                request.getType(),
                request.getStatus(),
                request.getCreatedAt(),
                request.getUpdatedAt(),
                commission,
                request.getMessages().stream().map(RecognitionMessageDto::fromEntity).toList()
        );
    }

    static String fullName(User user) {
        if (user.getProfile() == null) {
            return user.getEmail();
        }
        return (user.getProfile().getName() + " " + user.getProfile().getSurname()).trim();
    }
}
