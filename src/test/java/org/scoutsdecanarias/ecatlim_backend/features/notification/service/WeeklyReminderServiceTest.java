package org.scoutsdecanarias.ecatlim_backend.features.notification.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoutsdecanarias.ecatlim_backend.features.activity.dto.PendingActivityRow;
import org.scoutsdecanarias.ecatlim_backend.features.activity.enums.ProgressStatus;
import org.scoutsdecanarias.ecatlim_backend.features.activity.repository.ActivityProgressRepository;
import org.scoutsdecanarias.ecatlim_backend.features.notification.entity.Notification;
import org.scoutsdecanarias.ecatlim_backend.features.notification.enums.NotificationType;
import org.scoutsdecanarias.ecatlim_backend.features.notification.repository.NotificationRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.scoutsdecanarias.ecatlim_backend.shared.email.EmailService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeeklyReminderServiceTest {

    @Mock ActivityProgressRepository activityProgressRepository;
    @Mock NotificationRepository notificationRepository;
    @Mock UserRepository userRepository;
    @Mock EmailService emailService;

    @InjectMocks WeeklyReminderService service;

    private User user(int id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        return user;
    }

    private Notification notification(User user, String title) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(NotificationType.EVENT_PUBLISHED);
        notification.setTitle(title);
        return notification;
    }

    @Test
    void remindsOnlyUsersWithPendingThingsWhoAcceptEmails() {
        User withActivity = user(1, "a@test.org");
        User optedOut = user(2, "b@test.org");
        optedOut.setEmailRemindersEnabled(false);
        User withNews = user(3, "c@test.org");

        when(activityProgressRepository.findAvailableByStatus(eq(ProgressStatus.PENDING), any()))
                .thenReturn(List.of(
                        new PendingActivityRow(1, "Foro inicial", LocalDateTime.now().plusDays(2), "Curso"),
                        new PendingActivityRow(2, "Foro inicial", LocalDateTime.now().plusDays(2), "Curso")));
        when(notificationRepository.findAllPendingExcludingType(NotificationType.ACTIVITY_ASSIGNED))
                .thenReturn(List.of(notification(withNews, "Nuevo evento: Curso")));
        when(userRepository.findAllById(Set.of(1, 2, 3))).thenReturn(List.of(withActivity, optedOut, withNews));

        service.sendWeeklyReminders();

        verify(emailService).sendWeeklyReminderEmail(eq("a@test.org"), anyString(), anyList(), eq(List.of()));
        verify(emailService).sendWeeklyReminderEmail(eq("c@test.org"), anyString(), eq(List.of()), eq(List.of("Nuevo evento: Curso")));
        verify(emailService, never()).sendWeeklyReminderEmail(eq("b@test.org"), anyString(), anyList(), anyList());
    }

    @Test
    void sendsNothingWhenNobodyHasPendingThings() {
        when(activityProgressRepository.findAvailableByStatus(eq(ProgressStatus.PENDING), any())).thenReturn(List.of());
        when(notificationRepository.findAllPendingExcludingType(NotificationType.ACTIVITY_ASSIGNED)).thenReturn(List.of());
        when(userRepository.findAllById(Set.of())).thenReturn(List.of());

        service.sendWeeklyReminders();

        verify(emailService, never()).sendWeeklyReminderEmail(anyString(), anyString(), anyList(), anyList());
    }
}
