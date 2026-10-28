package org.scoutsdecanarias.ecatlim_backend.features.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.features.user.service.PendingUserService;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Every day, tells each head of education which people of their scout group are still waiting for their
 * registration request to be accepted, by email. The in-app notification of each request is created with it.
 * Requests of entities without an active head of education are sent to the ECATLIM address instead.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ecatlim.pending-users-digest.enabled", havingValue = "true", matchIfMissing = true)
public class PendingUserDigestService {

    private final PendingUserRepository pendingUserRepository;
    private final UserRepository userRepository;
    private final PendingUserService pendingUserService;
    private final EmailService emailService;

    @Scheduled(cron = "${ecatlim.pending-users-digest.cron:0 0 20 * * *}", zone = "Atlantic/Canary")
    @Transactional
    public void sendDailyDigest() {
        log.info("METHOD sendDailyDigest() - Starting pending registration requests digest");
        int sent = 0;
        Set<Integer> groupsWithHead = new HashSet<>();
        for (User head : userRepository.findAllByRolesContaining(Role.HEAD_OF_EDUCATION)) {
            if (!head.isEnabled() || head.getProfile() == null || head.getProfile().getScoutGroup() == null) {
                continue;
            }
            ScoutGroup scoutGroup = head.getProfile().getScoutGroup();
            groupsWithHead.add(scoutGroup.getId());
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
        notifyUnattendedRequests(groupsWithHead);
    }

    /** Requests nobody can accept because their entity has no active head of education go to the ECATLIM address. */
    private void notifyUnattendedRequests(Set<Integer> groupsWithHead) {
        Map<String, List<String>> requestsByGroup = new LinkedHashMap<>();
        for (PendingUser request : pendingUserRepository.findAll()) {
            ScoutGroup scoutGroup = request.getScoutGroup();
            if (scoutGroup != null && groupsWithHead.contains(scoutGroup.getId())) {
                continue;
            }
            String groupName = scoutGroup == null ? "Sin entidad de procedencia" : scoutGroup.getName() + " " + scoutGroup.getGroupNumber();
            requestsByGroup.computeIfAbsent(groupName, key -> new ArrayList<>())
                    .add(request.getName() + " " + request.getSurname() + " (" + request.getEmail() + ")");
        }
        if (requestsByGroup.isEmpty()) {
            return;
        }
        try {
            emailService.sendUnattendedPendingUserRequestsEmail(requestsByGroup);
        } catch (RuntimeException e) {
            log.error("METHOD notifyUnattendedRequests() - Could not send the unattended requests email", e);
        }
    }

    private void notifyHead(User head, ScoutGroup scoutGroup, List<PendingUser> pending) {
        List<String> descriptions = pending.stream()
                .map(request -> request.getName() + " " + request.getSurname() + " (" + request.getEmail() + ")")
                .toList();
        String groupName = scoutGroup.getName() + " " + scoutGroup.getGroupNumber();

        // Each request has its own notification, created when it was made; this only covers any that lack one
        pending.forEach(request -> pendingUserService.notifyHeadIfMissing(head, request));

        emailService.sendPendingUserRequestsDigestEmail(head.getEmail(),
                head.getProfile().getName() != null ? head.getProfile().getName() : head.getEmail(),
                groupName, descriptions);
    }
}
