package org.scoutsdecanarias.ecatlim_backend.features.user.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.scoutsdecanarias.ecatlim_backend.features.event.service.EnrollmentService;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.UserMeFormDto;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.UserProfile;
import org.scoutsdecanarias.ecatlim_backend.features.user.enums.Role;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserProfileRepository;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock UserProfileRepository userProfileRepository;
    @Mock EnrollmentService enrollmentService;

    @InjectMocks UserService userService;

    @BeforeEach
    void logIn() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@test.com", null, List.of()));
    }

    @AfterEach
    void logOut() {
        SecurityContextHolder.clearContext();
    }

    private User user(int id, String email, Role... roles) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.getRoles().addAll(List.of(roles));
        return user;
    }

    @Test
    void adminCannotDeactivateTheirOwnUser() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user(1, "admin@test.com", Role.ADMIN)));

        assertThatThrownBy(() -> userService.deactivateUser(1))
                .isInstanceOf(EcatlimException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(enrollmentService);
    }

    @Test
    void cannotDeactivateTheLastActiveAdmin() {
        User lastAdmin = user(2, "other@test.com", Role.ADMIN);
        when(userRepository.findById(2)).thenReturn(Optional.of(lastAdmin));
        when(userRepository.findAllByRolesContaining(Role.ADMIN)).thenReturn(List.of(lastAdmin));

        assertThatThrownBy(() -> userService.deactivateUser(2))
                .isInstanceOf(EcatlimException.class)
                .extracting("status").isEqualTo(HttpStatus.CONFLICT);
        verifyNoInteractions(enrollmentService);
    }

    @Test
    void deactivatesAUserAndReleasesTheirUpcomingEnrollments() {
        User student = user(3, "student@test.com", Role.STUDENT);
        when(userRepository.findById(3)).thenReturn(Optional.of(student));
        when(userRepository.save(student)).thenReturn(student);

        userService.deactivateUser(3);

        verify(enrollmentService).releaseUpcomingEnrollments(3);
    }

    @Test
    void cannotTakeTheNifOfAnotherUserWhenEditingMyProfile() {
        User me = user(1, "admin@test.com", Role.STUDENT);
        UserProfile otherProfile = new UserProfile();
        otherProfile.setUser(user(9, "other@test.com", Role.STUDENT));
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(me));
        when(userProfileRepository.findByNif("12345678Z")).thenReturn(Optional.of(otherProfile));

        UserMeFormDto form = new UserMeFormDto("A", "B", null, "12345678Z", null, null, null, null);

        assertThatThrownBy(() -> userService.updateUserMe(form))
                .isInstanceOf(EcatlimException.class)
                .extracting("status").isEqualTo(HttpStatus.CONFLICT);
        verify(userRepository, never()).save(any());
    }
}
