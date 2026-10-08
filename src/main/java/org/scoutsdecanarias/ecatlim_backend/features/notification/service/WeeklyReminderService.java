package org.scoutsdecanarias.ecatlim_backend.features.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.features.activity.dto.PendingActivityRow;
import org.scoutsdecanarias.ecatlim_backend.features.activity.enums.ProgressStatus;
import org.scoutsdecanarias.ecatlim_backend.features.activity.repository.ActivityProgressRepository;
import org.scoutsdecanarias.ecatlim_backend.features.notification.entity.Notification;
import org.scoutsdecanarias.ecatlim_backend.features.notification.enums.NotificationType;
import org.scoutsdecanarias.ecatlim_backend.features.notification.repository.NotificationRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.scoutsdecanarias.ecatlim_backend.shared.email.EmailService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Sends every Monday an email to the users who still have something pending.
 * Pending activities are read from the activity progress (so activities created before notifications existed are
 * included); everything else comes from the notifications that are unread or whose action is not fulfilled yet.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ecatlim.reminders.enabled", havingValue = "true", matchIfMissing = true)
public class WeeklyReminderService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ActivityProgressRepository activityProgressRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Scheduled(cron = "${ecatlim.reminders.cron:0 0 9 * * MON}", zone = "Atlantic/Canary")
    @Transactional(readOnly = true)
    public void sendWeeklyReminders() {
        log.info("METHOD sendWeeklyReminders() - Starting weekly reminders");
        LocalDateTime now = LocalDateTime.now();

        Map<Integer, List<String>> activitiesByUser = new HashMap<>();
        for (PendingActivityRow row : activityProgressRepository.findAvailableByStatus(ProgressStatus.PENDING, now)) {
            activitiesByUser.computeIfAbsent(row.studentId(), id -> new ArrayList<>()).add(describeActivity(row, now));
        }

        Map<Integer, List<String>> othersByUser = new HashMap<>();
        for (Notification notification : notificationRepository.findAllPendingExcludingType(NotificationType.ACTIVITY_ASSIGNED)) {
            othersByUser.computeIfAbsent(notification.getUser().getId(), id -> new ArrayList<>()).add(describeNotification(notification));
        }

        Set<Integer> candidates = new HashSet<>(activitiesByUser.keySet());

        candidates.addAll(othersByUser.keySet());

        int sent = 0;
        for (User user : userRepository.findAllById(candidates)) {
            if (!user.isEnabled() || !user.isEmailRemindersEnabled()) {
                continue;
            }
            try {
                emailService.sendWeeklyReminderEmail(
                        user.getEmail(),
                        user.getProfile() != null ? user.getProfile().getName() : user.getEmail(),
                        activitiesByUser.getOrDefault(user.getId(), List.of()),
                        othersByUser.getOrDefault(user.getId(), List.of()));
                sent++;
            } catch (RuntimeException e) {
                log.error("METHOD sendWeeklyReminders() - Could not queue reminder for user {}", user.getId(), e);
            }
        }
        log.info("METHOD sendWeeklyReminders() - {} reminders queued", sent);
    }

    private String describeActivity(PendingActivityRow row, LocalDateTime now) {
        StringBuilder text = new StringBuilder(row.activityTitle());
        if (row.eventTitle() != null) {
            text.append(" (").append(row.eventTitle()).append(")");
        }
        if (row.dueDate() != null) {
            text.append(row.dueDate().isBefore(now) ? " - venció el " : " - vence el ")
                    .append(row.dueDate().format(DATE_FORMAT));
        }
        return text.toString();
    }

    private String describeNotification(Notification notification) {
        return notification.getDescription() == null || notification.getDescription().isBlank()
                ? notification.getTitle()
                : notification.getTitle() + ": " + notification.getDescription();
    }
}
