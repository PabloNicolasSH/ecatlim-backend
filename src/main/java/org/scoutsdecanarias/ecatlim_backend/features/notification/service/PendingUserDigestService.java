package org.scoutsdecanarias.ecatlim_backend.features.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.features.notification.enums.NotificationType;
import org.scoutsdecanarias.ecatlim_backend.features.scout_group.ScoutGroup;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.PendingUser;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.enums.Role;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.PendingUserRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.scoutsdecanarias.ecatlim_backend.shared.email.EmailService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Every day, tells each head of education which people of their scout group are still waiting for their
 * registration request to be accepted, by email and with an in-app notification.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ecatlim.pending-users-digest.enabled", havingValue = "true", matchIfMissing = true)
public class PendingUserDigestService {

    private static final String REQUESTS_LINK = "/app/responsable-formacion/solicitudes-alta";

    private final PendingUserRepository pendingUserRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Scheduled(cron = "${ecatlim.pending-users-digest.cron:0 0 20 * * *}", zone = "Atlantic/Canary")
    @Transactional
    public void sendDailyDigest() {
        log.info("METHOD sendDailyDigest() - Starting pending registration requests digest");
        int sent = 0;
        for (User head : userRepository.findAllByRolesContaining(Role.HEAD_OF_EDUCATION)) {
            if (!head.isEnabled() || head.getProfile() == null || head.getProfile().getScoutGroup() == null) {
                continue;
            }
            ScoutGroup scoutGroup = head.getProfile().getScoutGroup();
            List<PendingUser> pending = pendingUserRepository.findAllByScoutGroup(scoutGroup);
            if (pending.isEmpty()) {
                continue;
            }
            try {
                notifyHead(head, scoutGroup, pending);
                sent++;
            } catch (RuntimeException e) {
                log.error("METHOD sendDailyDigest() - Could not notify head of education {}", head.getId(), e);
            }
        }
        log.info("METHOD sendDailyDigest() - {} heads of education notified", sent);
    }

    private void notifyHead(User head, ScoutGroup scoutGroup, List<PendingUser> pending) {
        List<String> descriptions = pending.stream()
                .map(request -> request.getName() + " " + request.getSurname() + " (" + request.getEmail() + ")")
                .toList();
        String groupName = scoutGroup.getName() + " " + scoutGroup.getGroupNumber();

        // Replace yesterday's notification so the bell never piles up one per day
        notificationService.resolve(head.getId(), NotificationType.PENDING_USER_REQUESTS, scoutGroup.getId());
        notificationService.notifyUsers(List.of(head.getId()), NotificationType.PENDING_USER_REQUESTS,
                pending.size() == 1 ? "1 solicitud de alta pendiente" : pending.size() + " solicitudes de alta pendientes",
                abbreviate(String.join(", ", descriptions)), REQUESTS_LINK, true, scoutGroup.getId());

        emailService.sendPendingUserRequestsDigestEmail(head.getEmail(),
                head.getProfile().getName() != null ? head.getProfile().getName() : head.getEmail(),
                groupName, descriptions);
    }

    /** The notification description column holds 500 characters. */
    private static String abbreviate(String text) {
        return text.length() <= 500 ? text : text.substring(0, 497) + "...";
    }
}
