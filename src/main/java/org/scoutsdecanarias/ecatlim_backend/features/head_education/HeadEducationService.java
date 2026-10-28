package org.scoutsdecanarias.ecatlim_backend.features.head_education;

import lombok.RequiredArgsConstructor;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.scoutsdecanarias.ecatlim_backend.features.enrollment.UserEnrollmentDetailDto;
import org.scoutsdecanarias.ecatlim_backend.features.event.dto.AttendedEventDto;
import org.scoutsdecanarias.ecatlim_backend.features.event.entity.Event;
import org.scoutsdecanarias.ecatlim_backend.features.event.entity.EventConfiguration;
import org.scoutsdecanarias.ecatlim_backend.features.event.entity.EventEnrollment;
import org.scoutsdecanarias.ecatlim_backend.features.event.enums.PaymentStatus;
import org.scoutsdecanarias.ecatlim_backend.features.event.repository.EventEnrollmentRepository;
import org.scoutsdecanarias.ecatlim_backend.features.event.service.EnrollmentService;
import org.scoutsdecanarias.ecatlim_backend.features.recognition.service.RecognitionService;
import org.scoutsdecanarias.ecatlim_backend.features.head_education.dto.HeadEducationEventDto;
import org.scoutsdecanarias.ecatlim_backend.features.head_education.dto.HeadEducationMemberDto;
import org.scoutsdecanarias.ecatlim_backend.features.head_education.dto.HeadEducationMemberHistoryDto;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.dto.LessonBlockCalendarSummaryDto;
import org.scoutsdecanarias.ecatlim_backend.features.scout_group.ScoutGroup;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.SimpleUserDto;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.enums.Role;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.service.StudentOverviewService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HeadEducationService {

    private static final Comparator<HeadEducationMemberDto> BY_NAME =
            Comparator.comparing((HeadEducationMemberDto m) -> lower(m.surname())).thenComparing(m -> lower(m.name()));

    private final UserRepository userRepository;
    private final EventEnrollmentRepository eventEnrollmentRepository;
    private final EnrollmentService enrollmentService;
    private final RecognitionService recognitionService;
    private final StudentOverviewService studentOverviewService;

    @Transactional(readOnly = true)
    public List<HeadEducationMemberDto> getMembers(String headEmail) {
        ScoutGroup group = getGroup(headEmail);
        return userRepository.findAllByProfile_ScoutGroup(group).stream()
                .filter(user -> user.getRoles().contains(Role.STUDENT))
                .map(this::toMemberDto)
                .sorted(BY_NAME)
                .toList();
    }

    @Transactional(readOnly = true)
    public HeadEducationMemberHistoryDto getMemberHistory(String headEmail, Integer memberId) {
        User member = getGroupMember(headEmail, memberId);

        List<UserEnrollmentDetailDto> stages = enrollmentService.getUserProgress(member.getEmail());

        Map<String, List<EventEnrollment>> byStage = eventEnrollmentRepository.findAttendedByUserId(member.getId()).stream()
                .collect(Collectors.groupingBy(ee -> ee.getLessonBlock().getModule().getEducationStage().getName(),
                        LinkedHashMap::new, Collectors.toList()));
        List<HeadEducationMemberHistoryDto.StageEventsDto> attendedEvents = byStage.entrySet().stream()
                .map(stageEntry -> {
                    Map<Event, List<EventEnrollment>> byEvent = stageEntry.getValue().stream()
                            .collect(Collectors.groupingBy(EventEnrollment::getEvent, LinkedHashMap::new, Collectors.toList()));
                    List<AttendedEventDto> events = byEvent.entrySet().stream()
                            .map(entry -> {
                                Event event = entry.getKey();
                                List<LessonBlockCalendarSummaryDto> blocks = entry.getValue().stream()
                                        .map(ee -> LessonBlockCalendarSummaryDto.fromEntity(ee.getLessonBlock()))
                                        .sorted(Comparator.comparing(LessonBlockCalendarSummaryDto::code))
                                        .toList();
                                return new AttendedEventDto(event.getId(), event.getTitle(), event.getStartDate(),
                                        event.getEndDate(), event.getLocation(), blocks);
                            })
                            .toList();
                    return new HeadEducationMemberHistoryDto.StageEventsDto(stageEntry.getKey(), events);
                })
                .toList();

        return new HeadEducationMemberHistoryDto(toMemberDto(member), stages, attendedEvents);
    }

    @Transactional
    public HeadEducationMemberDto setMemberEnabled(String headEmail, Integer memberId, boolean enabled) {
        User member = getGroupMember(headEmail, memberId);
        if (!enabled && member.getEmail().equals(headEmail)) {
            throw new EcatlimException("No puedes desactivar tu propio usuario", HttpStatus.BAD_REQUEST);
        }
        member.setEnabled(enabled);
        if (!enabled) {
            enrollmentService.releaseUpcomingEnrollments(member.getId());
            recognitionService.removeFromActiveCommissions(member.getId());
        }
        return toMemberDto(userRepository.save(member));
    }

    @Transactional(readOnly = true)
    public List<HeadEducationEventDto> getEvents(String headEmail) {
        ScoutGroup group = getGroup(headEmail);
        Map<Event, List<EventEnrollment>> byEvent = eventEnrollmentRepository.findAllByUserScoutGroup(group.getId()).stream()
                .collect(Collectors.groupingBy(EventEnrollment::getEvent, LinkedHashMap::new, Collectors.toList()));

        return byEvent.entrySet().stream()
                .map(entry -> {
                    Event event = entry.getKey();
                    Map<User, List<EventEnrollment>> byUser = entry.getValue().stream()
                            .collect(Collectors.groupingBy(EventEnrollment::getUser, LinkedHashMap::new, Collectors.toList()));
                    List<HeadEducationEventDto.AttendeeDto> attendees = byUser.entrySet().stream()
                            .map(userEntry -> {
                                SimpleUserDto simple = SimpleUserDto.fromEntity(userEntry.getKey());
                                List<LessonBlockCalendarSummaryDto> blocks = userEntry.getValue().stream()
                                        .map(ee -> LessonBlockCalendarSummaryDto.fromEntity(ee.getLessonBlock()))
                                        .sorted(Comparator.comparing(LessonBlockCalendarSummaryDto::code))
                                        .toList();
                                return new HeadEducationEventDto.AttendeeDto(simple.id(), simple.name(), simple.surname(),
                                        simple.email(), simple.avatarUrl(), paymentStatusOf(userEntry.getValue()).name(), blocks);
                            })
                            .sorted(Comparator.comparing((HeadEducationEventDto.AttendeeDto a) -> lower(a.surname()))
                                    .thenComparing(a -> lower(a.name())))
                            .toList();
                    EventConfiguration config = event.getEventConfiguration();
                    Integer cost = config == null ? null : config.getCost();
                    int unitCost = cost == null ? 0 : cost;
                    int payers = (int) attendees.stream().filter(a -> !PaymentStatus.REFUNDED.name().equals(a.paymentStatus())).count();
                    int paidPayers = (int) attendees.stream().filter(a -> PaymentStatus.PAID.name().equals(a.paymentStatus())).count();
                    return new HeadEducationEventDto(event.getId(), event.getTitle(), event.getStartDate(),
                            event.getEndDate(), event.getLocation(),
                            config == null ? null : config.getDateCloseInscription(), event.isClosed(),
                            config == null ? null : config.getTransferBankNumber(),
                            config == null ? null : config.getTransferCode(),
                            cost, unitCost * payers, unitCost * paidPayers,
                            unitCost * (payers - paidPayers), attendees);
                })
                .toList();
    }

    private ScoutGroup getGroup(String headEmail) {
        User head = userRepository.findByEmail(headEmail)
                .orElseThrow(() -> new EcatlimException("Usuario no encontrado", HttpStatus.NOT_FOUND));
        if (head.getProfile() == null || head.getProfile().getScoutGroup() == null) {
            throw new EcatlimException("No perteneces a ninguna entidad", HttpStatus.BAD_REQUEST);
        }
        return head.getProfile().getScoutGroup();
    }

    private User getGroupMember(String headEmail, Integer memberId) {
        ScoutGroup group = getGroup(headEmail);
        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new EcatlimException("Usuario no encontrado", HttpStatus.NOT_FOUND));
        boolean inGroup = member.getProfile() != null
                && member.getProfile().getScoutGroup() != null
                && member.getProfile().getScoutGroup().getId().equals(group.getId());
        if (!inGroup || !member.getRoles().contains(Role.STUDENT)) {
            throw new EcatlimException("Este usuario no pertenece a tu entidad", HttpStatus.FORBIDDEN);
        }
        return member;
    }

    private HeadEducationMemberDto toMemberDto(User user) {
        SimpleUserDto simple = SimpleUserDto.fromEntity(user);
        return new HeadEducationMemberDto(user.getId(), simple.name(), simple.surname(), user.getEmail(),
                simple.avatarUrl(), user.isEnabled(), studentOverviewService.currentStageOf(user));
    }

    private static PaymentStatus paymentStatusOf(List<EventEnrollment> rows) {
        if (rows.stream().anyMatch(r -> r.getPaymentStatus() == PaymentStatus.PENDING)) {
            return PaymentStatus.PENDING;
        }
        if (rows.stream().allMatch(r -> r.getPaymentStatus() == PaymentStatus.REFUNDED)) {
            return PaymentStatus.REFUNDED;
        }
        return PaymentStatus.PAID;
    }

    private static String lower(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}
