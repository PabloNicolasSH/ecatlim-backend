package org.scoutsdecanarias.ecatlim_backend.features.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.scoutsdecanarias.ecatlim_backend.core.exception.PendingUserExistsException;
import org.scoutsdecanarias.ecatlim_backend.features.scout_group.ScoutGroupService;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.PendingUserFormDto;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.PendingUser;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.UserProfile;
import org.scoutsdecanarias.ecatlim_backend.features.user.enums.Role;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.PendingUserRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserProfileRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.scoutsdecanarias.ecatlim_backend.shared.email.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class PendingUserService {

    private final PendingUserRepository pendingUserRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final ScoutGroupService scoutGroupService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

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

        if (userProfileRepository.findByNif(pendingUser.nif()).isPresent()) {
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
        newUser.setEmail(pendingUser.getEmail());
        newUser.setRole(Role.STUDENT);

        PasswordGenerator passwordGenerator = new PasswordGenerator();
        String password = passwordGenerator.generatePassword(12, new CharacterRule(EnglishCharacterData.Alphabetical, 7), new CharacterRule(EnglishCharacterData.Digit, 3));
        newUser.setPassword(passwordEncoder.encode(password));

        UserProfile  newUserProfile = new UserProfile();
        newUserProfile.setUser(newUser);
        newUserProfile.setName(pendingUser.getName());
        newUserProfile.setSurname(pendingUser.getSurname());

        newUserProfile.setNif(pendingUser.getNif());
        newUserProfile.setScoutGroup(pendingUser.getScoutGroup());

        newUser.setProfile(newUserProfile);

        emailService.sendWelcomeEmail(pendingUserFormDto.email(), pendingUserFormDto.name(), password);
        userRepository.save(newUser);
        pendingUserRepository.delete(pendingUser);
    }

    public void deletePendingUser(PendingUserFormDto pendingUserFormDto) {
        if (pendingUserRepository.findByEmail(pendingUserFormDto.email()).isPresent()) {
            pendingUserRepository.delete(pendingUserRepository.findByEmail(pendingUserFormDto.email()).get());
        }
    }
}
