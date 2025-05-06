package org.scoutsdecanarias.ecatlim_backend.service;

import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.dto.PendingUserFormDto;
import org.scoutsdecanarias.ecatlim_backend.entity.PendingUser;
import org.scoutsdecanarias.ecatlim_backend.exception.PendingUserExistsException;
import org.scoutsdecanarias.ecatlim_backend.repository.PendingUserRepository;
import org.scoutsdecanarias.ecatlim_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PendingUserService {

    private final PendingUserRepository pendingUserRepository;
    private final UserRepository userRepository;
    private final ScoutGroupService scoutGroupService;
    private final EmailService emailService;

    public PendingUserService(PendingUserRepository pendingUserRepository, UserRepository userRepository, ScoutGroupService scoutGroupService, EmailService emailService) {
        this.pendingUserRepository = pendingUserRepository;
        this.userRepository = userRepository;
        this.scoutGroupService = scoutGroupService;
        this.emailService = emailService;
    }

    public void addPendingUser(PendingUserFormDto pendingUser) {

        if (userRepository.findByEmail(pendingUser.email()).isPresent()) {
            throw new PendingUserExistsException("Email already registered as user");
        }

        if (pendingUserRepository.findByEmail(pendingUser.email()).isPresent()) {
            throw new PendingUserExistsException("Email already in pending requests");
        }

        PendingUser newPendingUser = new PendingUser();
        newPendingUser.setName(pendingUser.name());
        newPendingUser.setSurname(pendingUser.surname());
        newPendingUser.setEmail(pendingUser.email());

        if (pendingUser.scoutGroupId() != null) {
            newPendingUser.setScoutGroup(scoutGroupService.getScoutGroupById(pendingUser.scoutGroupId()));
        }

        emailService.sendPendingUserCreatedEmail(newPendingUser.getEmail(), newPendingUser.getName());
        pendingUserRepository.save(newPendingUser);
    }
}
