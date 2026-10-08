package org.scoutsdecanarias.ecatlim_backend.features.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.core.exception.ResourceNotFoundException;
import org.scoutsdecanarias.ecatlim_backend.features.notification.dto.NotificationDto;
import org.scoutsdecanarias.ecatlim_backend.features.notification.entity.Notification;
import org.scoutsdecanarias.ecatlim_backend.features.notification.enums.NotificationType;
import org.scoutsdecanarias.ecatlim_backend.features.notification.repository.NotificationRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private static final String NEW_QUEUE = "/queue/notifications";
    private static final String UPDATED_QUEUE = "/queue/notifications-updated";

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate template;

    public void notifyUsers(Collection<Integer> userIds, NotificationType type, String title, String description,
                            String link, boolean requiresAction, Integer referenceId) {
        userRepository.findAllById(userIds).forEach(user -> {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setType(type);
            notification.setTitle(title);
            notification.setDescription(description);
            notification.setLink(link);
            notification.setRequiresAction(requiresAction);
            notification.setReferenceId(referenceId);
            Notification saved = notificationRepository.save(notification);
            pushAfterCommit(user.getEmail(), NEW_QUEUE, NotificationDto.from(saved));
        });
    }

    /**
     * Marks as fulfilled the action notifications of a user tied to a given reference.
     */
    public void resolve(Integer userId, NotificationType type, Integer referenceId) {
        resolveNotifications(notificationRepository.findByUserIdAndTypeAndReferenceIdAndResolvedAtIsNull(userId, type, referenceId));
    }

    /**
     * Marks as fulfilled the action notifications of any user tied to a given reference.
     */
    public void resolveAll(NotificationType type, Integer referenceId) {
        resolveNotifications(notificationRepository.findByTypeAndReferenceIdAndResolvedAtIsNull(type, referenceId));
    }

    private void resolveNotifications(List<Notification> notifications) {
        LocalDateTime now = LocalDateTime.now();
        notifications.forEach(notification -> {
            notification.setResolvedAt(now);
            if (notification.getReadAt() == null) {
                notification.setReadAt(now);
            }
            pushAfterCommit(notification.getUser().getEmail(), UPDATED_QUEUE, NotificationDto.from(notification));
        });
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getNotifications(String email, int page, int size) {
        User user = getUser(email);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(page, size))
                .stream().map(NotificationDto::from).toList();
    }

    @Transactional(readOnly = true)
    public long countPending(String email) {
        return notificationRepository.countPending(getUser(email).getId());
    }

    public void markAsRead(String email, Integer notificationId) {
        User user = getUser(email);
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));
        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
        }
    }

    public void markAllAsRead(String email) {
        LocalDateTime now = LocalDateTime.now();
        notificationRepository.findByUserIdAndReadAtIsNull(getUser(email).getId())
                .forEach(notification -> notification.setReadAt(now));
    }

    @Transactional(readOnly = true)
    public boolean isEmailRemindersEnabled(String email) {
        return getUser(email).isEmailRemindersEnabled();
    }

    public void setEmailRemindersEnabled(String email, boolean enabled) {
        getUser(email).setEmailRemindersEnabled(enabled);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private void pushAfterCommit(String email, String queue, NotificationDto dto) {
        Runnable push = () -> {
            try {
                template.convertAndSendToUser(email, queue, dto);
            } catch (RuntimeException e) {
                log.warn("METHOD pushAfterCommit() - Could not push notification {} to {}", dto.id(), email, e);
            }
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    push.run();
                }
            });
        } else {
            push.run();
        }
    }
}
