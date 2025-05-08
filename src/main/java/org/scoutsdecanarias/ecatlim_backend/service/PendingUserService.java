package org.scoutsdecanarias.ecatlim_backend.service;

import lombok.extern.slf4j.Slf4j;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.scoutsdecanarias.ecatlim_backend.dto.PendingUserFormDto;
import org.scoutsdecanarias.ecatlim_backend.entity.PendingUser;
import org.scoutsdecanarias.ecatlim_backend.entity.User;
import org.scoutsdecanarias.ecatlim_backend.enums.Role;
import org.scoutsdecanarias.ecatlim_backend.exception.PendingUserExistsException;
import org.scoutsdecanarias.ecatlim_backend.repository.PendingUserRepository;
import org.scoutsdecanarias.ecatlim_backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class PendingUserService {

    private final PendingUserRepository pendingUserRepository;
    private final UserRepository userRepository;
    private final ScoutGroupService scoutGroupService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PendingUserService(PendingUserRepository pendingUserRepository, UserRepository userRepository, ScoutGroupService scoutGroupService, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.pendingUserRepository = pendingUserRepository;
        this.userRepository = userRepository;
        this.scoutGroupService = scoutGroupService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public List<PendingUser> getAllPendingUsers() {
        return pendingUserRepository.findAll();
    }

    public void addPendingUser(PendingUserFormDto pendingUser) {

        if (userRepository.findByEmail(pendingUser.email()).isPresent()) {
            throw new PendingUserExistsException("Email already registered as user");
        }

        if (pendingUserRepository.findByEmail(pendingUser.email()).isPresent()) {
            throw new PendingUserExistsException("Email already in pending requests");
        }

        if (userRepository.findByNif(pendingUser.nif()).isPresent()) {
            throw new PendingUserExistsException("Nif already registered as a user");
        }

        PendingUser newPendingUser = new PendingUser();
        newPendingUser.setName(pendingUser.name());
        newPendingUser.setSurname(pendingUser.surname());
        newPendingUser.setEmail(pendingUser.email());
        newPendingUser.setNif(pendingUser.nif());

        if (pendingUser.scoutGroupId() != null) {
            newPendingUser.setScoutGroup(scoutGroupService.getScoutGroupById(pendingUser.scoutGroupId()));
        }

        emailService.sendPendingUserCreatedEmail(newPendingUser.getEmail(), newPendingUser.getName(), newPendingUser.getSurname(), newPendingUser.getNif(), newPendingUser.getScoutGroup());
        pendingUserRepository.save(newPendingUser);
    }

    public void createUserFromRequest(PendingUserFormDto pendingUserFormDto) {
        if (userRepository.findByEmail(pendingUserFormDto.email()).isPresent()) {
            throw new PendingUserExistsException("Email already registered as user");
        }

        PendingUser pendingUser = pendingUserRepository.findByEmail(pendingUserFormDto.email()).get();

        User newUser = new User();
        newUser.setName(pendingUser.getName());
        newUser.setSurname(pendingUser.getSurname());
        newUser.setEmail(pendingUser.getEmail());
        newUser.setNif(pendingUser.getNif());
        newUser.setScoutGroup(pendingUser.getScoutGroup());
        newUser.setRole(Role.STUDENT);

        PasswordGenerator passwordGenerator = new PasswordGenerator();
        String password = passwordGenerator.generatePassword(12, new CharacterRule(EnglishCharacterData.Alphabetical, 7), new CharacterRule(EnglishCharacterData.Digit, 3));
        newUser.setPassword(passwordEncoder.encode(password));

        emailService.sendWelcomeEmail(pendingUserFormDto.email(), pendingUserFormDto.name(), pendingUserFormDto.email(), password);
        userRepository.save(newUser);
        pendingUserRepository.delete(pendingUser);
    }

    public void deletePendingUser(PendingUserFormDto pendingUserFormDto) {
        if (pendingUserRepository.findByEmail(pendingUserFormDto.email()).isPresent()) {
            pendingUserRepository.delete(pendingUserRepository.findByEmail(pendingUserFormDto.email()).get());
        }
    }
}
