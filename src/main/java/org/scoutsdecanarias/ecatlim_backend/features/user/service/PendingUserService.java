package org.scoutsdecanarias.ecatlim_backend.features.user.service;

import org.scoutsdecanarias.ecatlim_backend.shared.utils.IdDocuments;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.scoutsdecanarias.ecatlim_backend.features.scout_group.ScoutGroup;
import org.scoutsdecanarias.ecatlim_backend.features.notification.enums.NotificationType;
import org.scoutsdecanarias.ecatlim_backend.features.notification.service.NotificationService;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class PendingUserService {

    private static final String REQUESTS_LINK = "/app/responsable-formacion";

    private final PendingUserRepository pendingUserRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final ScoutGroupService scoutGroupService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final NotificationService notificationService;

    public List<PendingUser> getAllPendingUsers() {
        return pendingUserRepository.findAll();
    }

    public List<PendingUser> getAllByHeadEducation(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        ScoutGroup scoutGroup = user.getProfile().getScoutGroup();
        if (scoutGroup == null) {
            return List.of();
        }

        return pendingUserRepository.findAllByScoutGroup(scoutGroup);
    }

    @Transactional
    public void addPendingUser(PendingUserFormDto pendingUser) {

        if (userRepository.findByEmail(pendingUser.email()).isPresent()) {
            throw new EcatlimException("Con este email ya se ha solicitado que se le dé de alta en el sistema", HttpStatus.CONFLICT);
        }

        if (pendingUserRepository.findByEmail(pendingUser.email()).isPresent()) {
            throw new EcatlimException("Con este email ya se ha solicitado que se le dé de alta en el sistema", HttpStatus.CONFLICT);
        }

        if (userProfileRepository.findByNif(IdDocuments.normalize(pendingUser.nif())).isPresent()) {
            throw new EcatlimException("Este NIF ya está de alta en el sistema", HttpStatus.CONFLICT);
        }

        if (pendingUserRepository.existsByNif(IdDocuments.normalize(pendingUser.nif()))) {
            throw new EcatlimException("Ya existe una solicitud de alta con este NIF", HttpStatus.CONFLICT);
        }

        PendingUser newPendingUser = new PendingUser();
        newPendingUser.setName(pendingUser.name());
        newPendingUser.setSurname(pendingUser.surname());
        newPendingUser.setEmail(pendingUser.email());
        newPendingUser.setNif(IdDocuments.normalize(pendingUser.nif()));

        if (pendingUser.scoutGroupId() != null) {
            newPendingUser.setScoutGroup(scoutGroupService.getScoutGroupById(pendingUser.scoutGroupId()));
        }

        emailService.sendPendingUserCreatedEmail(newPendingUser.getEmail(), newPendingUser.getName(), newPendingUser.getSurname(), newPendingUser.getNif(), newPendingUser.getScoutGroup());
        PendingUser saved = pendingUserRepository.save(newPendingUser);
        notifyHeadsOfEducation(saved);
    }

    /**
     * Each registration request gets its own notification for the heads of education of its scout group; it is
     * resolved for all of them as soon as one accepts or rejects the request.
     */
    private void notifyHeadsOfEducation(PendingUser request) {
        if (request.getScoutGroup() == null) {
            return;
        }
        List<Integer> headIds = userRepository.findAllByRolesContaining(Role.HEAD_OF_EDUCATION).stream()
                .filter(head -> head.isEnabled() && isInGroup(head, request.getScoutGroup()))
                .map(User::getId)
                .toList();
        notificationService.notifyUsers(headIds, NotificationType.PENDING_USER_REQUESTS,
                "Solicitud de alta pendiente", request.getName() + " " + request.getSurname() + " (" + request.getEmail() + ")",
                REQUESTS_LINK, true, request.getId());
    }

    /**
     * Creates the notification of a request for a head of education who does not have it yet, such as requests
     * made before notifications were per request or heads who joined the group later.
     */
    public void notifyHeadIfMissing(User head, PendingUser request) {
        if (!notificationService.exists(head.getId(), NotificationType.PENDING_USER_REQUESTS, request.getId())) {
            notificationService.notifyUsers(List.of(head.getId()), NotificationType.PENDING_USER_REQUESTS,
                    "Solicitud de alta pendiente", request.getName() + " " + request.getSurname() + " (" + request.getEmail() + ")",
                    REQUESTS_LINK, true, request.getId());
        }
    }

    private static boolean isInGroup(User user, ScoutGroup scoutGroup) {
        return user.getProfile() != null && user.getProfile().getScoutGroup() != null
                && user.getProfile().getScoutGroup().getId().equals(scoutGroup.getId());
    }

    @Transactional
    public void createUserFromRequest(PendingUserFormDto pendingUserFormDto) {
        if (userRepository.findByEmail(pendingUserFormDto.email()).isPresent()) {
            throw new EcatlimException("Con este email ya se ha solicitado que se le dé de alta en el sistema", HttpStatus.CONFLICT);
        }

        PendingUser pendingUser = pendingUserRepository.findByEmail(pendingUserFormDto.email())
                .orElseThrow(() -> new EcatlimException("No existe ninguna solicitud de alta con ese email", HttpStatus.NOT_FOUND));

        User newUser = new User();
        newUser.setEmail(pendingUser.getEmail());
        newUser.getRoles().add(Role.STUDENT);

        PasswordGenerator passwordGenerator = new PasswordGenerator();
        String password = passwordGenerator.generatePassword(12, new CharacterRule(EnglishCharacterData.Alphabetical, 7), new CharacterRule(EnglishCharacterData.Digit, 3));
        newUser.setPassword(passwordEncoder.encode(password));

        UserProfile newUserProfile = new UserProfile();
        newUserProfile.setUser(newUser);
        newUserProfile.setName(pendingUser.getName());
        newUserProfile.setSurname(pendingUser.getSurname());

        newUserProfile.setNif(pendingUser.getNif());
        newUserProfile.setScoutGroup(pendingUser.getScoutGroup());

        newUser.setProfile(newUserProfile);

        emailService.sendWelcomeEmail(pendingUserFormDto.email(), pendingUserFormDto.name(), password);
        userRepository.save(newUser);
        pendingUserRepository.delete(pendingUser);
        notificationService.resolveAll(NotificationType.PENDING_USER_REQUESTS, pendingUser.getId());
    }

    @Transactional
    public void createUserFromRequestByHeadEducation(String headEmail, PendingUserFormDto pendingUserFormDto) {
        assertRequestBelongsToHeadEducation(headEmail, pendingUserFormDto.email());
        createUserFromRequest(pendingUserFormDto);
    }

    @Transactional
    public void deletePendingUserByHeadEducation(String headEmail, PendingUserFormDto pendingUserFormDto) {
        assertRequestBelongsToHeadEducation(headEmail, pendingUserFormDto.email());
        deletePendingUser(pendingUserFormDto);
    }

    private void assertRequestBelongsToHeadEducation(String headEmail, String requestEmail) {
        User head = userRepository.findByEmail(headEmail).orElseThrow();
        ScoutGroup headGroup = head.getProfile() != null ? head.getProfile().getScoutGroup() : null;
        PendingUser pendingUser = pendingUserRepository.findByEmail(requestEmail)
                .orElseThrow(() -> new EcatlimException("No existe ninguna solicitud de alta con ese email", HttpStatus.NOT_FOUND));

        if (headGroup == null || pendingUser.getScoutGroup() == null
                || !headGroup.getId().equals(pendingUser.getScoutGroup().getId())) {
            throw new EcatlimException("Esta solicitud no pertenece a tu entidad", HttpStatus.FORBIDDEN);
        }
    }

    @Transactional
    public void deletePendingUser(PendingUserFormDto pendingUserFormDto) {
        if (pendingUserRepository.findByEmail(pendingUserFormDto.email()).isPresent()) {
            PendingUser pendingUser = pendingUserRepository.findByEmail(pendingUserFormDto.email()).get();
            pendingUserRepository.delete(pendingUser);
            notificationService.resolveAll(NotificationType.PENDING_USER_REQUESTS, pendingUser.getId());
        }
    }
}
