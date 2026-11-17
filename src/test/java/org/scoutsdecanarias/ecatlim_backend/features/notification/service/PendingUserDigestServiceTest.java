package org.scoutsdecanarias.ecatlim_backend.features.notification.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoutsdecanarias.ecatlim_backend.features.scout_group.ScoutGroup;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.PendingUser;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.UserProfile;
import org.scoutsdecanarias.ecatlim_backend.features.user.enums.Role;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.PendingUserRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.service.PendingUserService;
import org.scoutsdecanarias.ecatlim_backend.shared.email.EmailService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PendingUserDigestServiceTest {

    @Mock PendingUserRepository pendingUserRepository;
    @Mock UserRepository userRepository;
    @Mock PendingUserService pendingUserService;
    @Mock EmailService emailService;

    @InjectMocks PendingUserDigestService digestService;

    private static ScoutGroup group(int id, String name) {
        ScoutGroup group = new ScoutGroup();
        group.setId(id);
        group.setName(name);
        group.setGroupNumber(id);
        return group;
    }

    private static PendingUser request(String name, ScoutGroup group) {
        PendingUser request = new PendingUser();
        request.setName(name);
        request.setSurname("Apellido");
        request.setEmail(name.toLowerCase() + "@test.com");
        request.setScoutGroup(group);
        return request;
    }

    @Test
    @SuppressWarnings("unchecked")
    void requestsOfGroupsWithoutAnActiveHeadGoToTheEcatlimAddress() {
        ScoutGroup withHead = group(1, "Con responsable");
        ScoutGroup withoutHead = group(2, "Sin responsable");

        User head = new User();
        head.setId(10);
        head.setEmail("head@test.com");
        head.getRoles().add(Role.HEAD_OF_EDUCATION);
        UserProfile profile = new UserProfile();
        profile.setName("Jefa");
        profile.setScoutGroup(withHead);
        head.setProfile(profile);

        PendingUser attended = request("Ana", withHead);
        PendingUser unattended = request("Luis", withoutHead);
        PendingUser noGroup = request("Eva", null);

        when(userRepository.findAllByRolesContaining(Role.HEAD_OF_EDUCATION)).thenReturn(List.of(head));
        when(pendingUserRepository.findAllByScoutGroup(withHead)).thenReturn(List.of(attended));
        when(pendingUserRepository.findAll()).thenReturn(List.of(attended, unattended, noGroup));

        digestService.sendDailyDigest();

        verify(emailService).sendPendingUserRequestsDigestEmail(eq("head@test.com"), anyString(), anyString(), anyList());
        ArgumentCaptor<Map<String, List<String>>> captor = ArgumentCaptor.forClass(Map.class);
        verify(emailService).sendUnattendedPendingUserRequestsEmail(captor.capture());
        assertThat(captor.getValue()).containsOnlyKeys("Sin responsable 2", "Sin entidad de procedencia");
    }

    @Test
    void noUnattendedEmailWhenEveryRequestHasAHead() {
        ScoutGroup withHead = group(1, "Con responsable");
        User head = new User();
        head.setId(10);
        head.setEmail("head@test.com");
        UserProfile profile = new UserProfile();
        profile.setScoutGroup(withHead);
        head.setProfile(profile);
        PendingUser attended = request("Ana", withHead);

        when(userRepository.findAllByRolesContaining(Role.HEAD_OF_EDUCATION)).thenReturn(List.of(head));
        when(pendingUserRepository.findAllByScoutGroup(withHead)).thenReturn(List.of(attended));
        when(pendingUserRepository.findAll()).thenReturn(List.of(attended));

        digestService.sendDailyDigest();

        verify(emailService, never()).sendUnattendedPendingUserRequestsEmail(any());
    }
}
