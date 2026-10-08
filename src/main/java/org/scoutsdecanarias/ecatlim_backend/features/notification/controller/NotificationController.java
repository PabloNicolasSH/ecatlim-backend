package org.scoutsdecanarias.ecatlim_backend.features.notification.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.features.notification.dto.NotificationDto;
import org.scoutsdecanarias.ecatlim_backend.features.notification.dto.NotificationPreferencesDto;
import org.scoutsdecanarias.ecatlim_backend.features.notification.dto.PendingCountDto;
import org.scoutsdecanarias.ecatlim_backend.features.notification.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationDto> getNotifications(@RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "30") int size,
                                                  Principal principal) {
        log.info("METHOD getNotifications() - User {} listing notifications", principal.getName());
        return notificationService.getNotifications(principal.getName(), page, Math.min(size, 100));
    }

    @GetMapping("/pending-count")
    public PendingCountDto getPendingCount(Principal principal) {
        return new PendingCountDto(notificationService.countPending(principal.getName()));
    }

    @PostMapping("/{id}/read")
    public void markAsRead(@PathVariable Integer id, Principal principal) {
        log.info("METHOD markAsRead() - User {} reading notification {}", principal.getName(), id);
        notificationService.markAsRead(principal.getName(), id);
    }

    @PostMapping("/read-all")
    public void markAllAsRead(Principal principal) {
        log.info("METHOD markAllAsRead() - User {} reading all notifications", principal.getName());
        notificationService.markAllAsRead(principal.getName());
    }

    @GetMapping("/preferences")
    public NotificationPreferencesDto getPreferences(Principal principal) {
        return new NotificationPreferencesDto(notificationService.isEmailRemindersEnabled(principal.getName()));
    }

    @PutMapping("/preferences")
    public NotificationPreferencesDto updatePreferences(@RequestBody NotificationPreferencesDto preferences, Principal principal) {
        log.info("METHOD updatePreferences() - User {} sets email reminders to {}", principal.getName(), preferences.emailReminders());
        notificationService.setEmailRemindersEnabled(principal.getName(), preferences.emailReminders());
        return preferences;
    }
}
