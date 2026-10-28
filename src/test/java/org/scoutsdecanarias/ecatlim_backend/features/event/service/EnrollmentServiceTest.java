package org.scoutsdecanarias.ecatlim_backend.features.event.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.scoutsdecanarias.ecatlim_backend.features.event.dto.StudentEnrolledEvent;
import org.scoutsdecanarias.ecatlim_backend.features.event.dto.StudentUnenrolledEvent;
import org.scoutsdecanarias.ecatlim_backend.features.event.entity.Event;
import org.scoutsdecanarias.ecatlim_backend.features.event.entity.EventConfiguration;
import org.scoutsdecanarias.ecatlim_backend.features.event.entity.EventEnrollment;
import org.scoutsdecanarias.ecatlim_backend.features.event.enums.AttendanceType;
import org.scoutsdecanarias.ecatlim_backend.features.event.enums.EventStatus;
import org.scoutsdecanarias.ecatlim_backend.features.event.repository.EventEnrollmentRepository;
import org.scoutsdecanarias.ecatlim_backend.features.event.repository.EventRepository;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.LessonBlock;
import org.scoutsdecanarias.ecatlim_backend.features.lesson_block.UserLessonBlock;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.enums.Role;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserLessonBlockRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock UserRepository userRepository;
    @Mock EventRepository eventRepository;
    @Mock EventEnrollmentRepository eventEnrollmentRepository;
    @Mock UserLessonBlockRepository userLessonBlockRepository;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks EnrollmentService enrollmentService;

    private User student;
    private LessonBlock block;
    private Event event;

    @BeforeEach
    void setUp() {
        student = new User();
        student.setId(7);
        student.setEmail("student@test.com");
        student.getRoles().add(Role.STUDENT);

        block = new LessonBlock();
        block.setId(1);

        event = new Event();
        event.setId(3);
        event.setStatus(EventStatus.PUBLISHED);
        event.setStartDate(LocalDateTime.now().plusDays(10));
        event.setEndDate(LocalDateTime.now().plusDays(11));
        event.getLessonBlocks().add(block);

        lenient().when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(student));
        lenient().when(eventRepository.findById(3)).thenReturn(Optional.of(event));
        lenient().when(eventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void takingBlock(boolean completed) {
        UserLessonBlock progress = new UserLessonBlock();
        progress.setCompleted(completed);
        lenient().when(userLessonBlockRepository.findByUserIdAndLessonBlockId(7, 1)).thenReturn(Optional.of(progress));
    }

    private void assertRejected(HttpStatus status) {
        assertThatThrownBy(() -> enrollmentService.enrollStudent(3, "student@test.com", List.of(1)))
                .isInstanceOf(EcatlimException.class)
                .extracting("status").isEqualTo(status);
        assertThat(event.getEnrollments()).isEmpty();
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void enrollsInAnOpenPublishedEvent() {
        takingBlock(false);

        enrollmentService.enrollStudent(3, "student@test.com", List.of(1));

        assertThat(event.getEnrollments()).hasSize(1);
        verify(eventPublisher).publishEvent(any(StudentEnrolledEvent.class));
    }

    @Test
    void cannotEnrollInAnEventThatIsNotPublished() {
        takingBlock(false);
        event.setStatus(EventStatus.DRAFT);

        assertRejected(HttpStatus.CONFLICT);
    }

    @Test
    void cannotEnrollOutsideTheInscriptionWindow() {
        takingBlock(false);
        EventConfiguration config = new EventConfiguration();
        event.setEventConfiguration(config);

        config.setDateOpenInscription(LocalDateTime.now().plusDays(1));
        config.setDateCloseInscription(LocalDateTime.now().plusDays(5));
        assertRejected(HttpStatus.CONFLICT);

        config.setDateOpenInscription(LocalDateTime.now().minusDays(5));
        config.setDateCloseInscription(LocalDateTime.now().minusDays(1));
        assertRejected(HttpStatus.CONFLICT);
    }

    @Test
    void cannotEnrollInAnEventThatAlreadyEnded() {
        takingBlock(false);
        event.setEndDate(LocalDateTime.now().minusDays(1));

        assertRejected(HttpStatus.CONFLICT);
    }

    @Test
    void cannotEnrollInABlockOfAnotherEvent() {
        assertThatThrownBy(() -> enrollmentService.enrollStudent(3, "student@test.com", List.of(99)))
                .isInstanceOf(EcatlimException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void cannotEnrollWithoutTakingTheBlockOrAfterPassingIt() {
        assertRejected(HttpStatus.FORBIDDEN);

        takingBlock(true);
        assertRejected(HttpStatus.FORBIDDEN);
    }

    @Test
    void cannotUnenrollFromABlockWithRecordedAttendance() {
        EventEnrollment enrollment = new EventEnrollment();
        enrollment.setAttendance(AttendanceType.values()[0]);
        when(eventEnrollmentRepository.findByUserIdAndEventIdAndLessonBlockId(7, 3, 1)).thenReturn(Optional.of(enrollment));

        assertThatThrownBy(() -> enrollmentService.unenrollStudent(3, "student@test.com", List.of(1)))
                .isInstanceOf(EcatlimException.class)
                .extracting("status").isEqualTo(HttpStatus.CONFLICT);
        verify(eventEnrollmentRepository, never()).deleteByUserIdAndEventIdAndLessonBlockId(any(), any(), any());
    }

    @Test
    void cannotUnenrollFromABlockTheUserIsNotEnrolledIn() {
        when(eventEnrollmentRepository.findByUserIdAndEventIdAndLessonBlockId(7, 3, 1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.unenrollStudent(3, "student@test.com", List.of(1)))
                .isInstanceOf(EcatlimException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void unenrollingDropsTheEnrollmentAndAnnouncesIt() {
        when(eventEnrollmentRepository.findByUserIdAndEventIdAndLessonBlockId(7, 3, 1)).thenReturn(Optional.of(new EventEnrollment()));

        enrollmentService.unenrollStudent(3, "student@test.com", List.of(1));

        verify(eventPublisher).publishEvent(any(StudentUnenrolledEvent.class));
        verify(eventEnrollmentRepository).deleteByUserIdAndEventIdAndLessonBlockId(7, 3, 1);
    }
}
