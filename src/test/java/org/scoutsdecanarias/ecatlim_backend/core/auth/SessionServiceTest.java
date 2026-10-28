package org.scoutsdecanarias.ecatlim_backend.core.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoutsdecanarias.ecatlim_backend.core.UserDetailsServiceImpl;
import org.scoutsdecanarias.ecatlim_backend.core.auth.refresh.RefreshTokenService;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock RefreshTokenService refreshTokenService;
    @Mock UserDetailsServiceImpl userDetailsService;
    @Mock UserRepository userRepository;

    private JwtUtil jwtUtil;
    private User user;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", Base64.getEncoder().encodeToString(new byte[32]));
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 60_000L);

        user = new User();
        user.setId(7);
        user.setEmail("user@test.com");
        lenient().when(userDetailsService.loadUserByUsername("user@test.com"))
                .thenReturn(new org.springframework.security.core.userdetails.User("user@test.com", "hash", List.of()));
        lenient().when(refreshTokenService.issue(user)).thenReturn("refresh");
    }

    private SessionService service(int maxAgeDays) {
        return new SessionService(jwtUtil, refreshTokenService, userDetailsService, userRepository, maxAgeDays);
    }

    @Test
    void aLoginIsRecordedButARenewalIsNot() {
        service(90).renew(user);
        assertThat(user.getLastLoginAt()).isNull();

        service(90).open(user, null);
        assertThat(user.getLastLoginAt()).isCloseTo(LocalDateTime.now(), org.assertj.core.api.Assertions.within(5, java.time.temporal.ChronoUnit.SECONDS));
    }

    @Test
    void aRecentPasswordKeepsTheSessionNormal() {
        user.setPasswordChangedAt(LocalDateTime.now().minusDays(89));

        AuthResponse response = service(90).open(user, null);

        assertThat(response.passwordChangeRequired()).isFalse();
        assertThat(user.isMustChangePassword()).isFalse();
        assertThat(jwtUtil.isPasswordChangeRequired(response.token())).isFalse();
    }

    @Test
    void anOldPasswordForcesAChangeAtLogin() {
        user.setPasswordChangedAt(LocalDateTime.now().minusDays(91));

        AuthResponse response = service(90).open(user, null);

        assertThat(response.passwordChangeRequired()).isTrue();
        assertThat(user.isMustChangePassword()).isTrue();
        assertThat(jwtUtil.isPasswordChangeRequired(response.token())).isTrue();
        verify(userRepository).save(user);
    }

    @Test
    void anOpenSessionIsRestrictedWhenItsPasswordExpiresDuringRenewal() {
        user.setPasswordChangedAt(LocalDateTime.now().minusDays(120));

        AuthResponse response = service(90).renew(user);

        assertThat(response.passwordChangeRequired()).isTrue();
    }

    @Test
    void zeroDaysTurnsTheRuleOff() {
        user.setPasswordChangedAt(LocalDateTime.now().minusDays(1000));

        assertThat(service(0).open(user, null).passwordChangeRequired()).isFalse();
    }

    @Test
    void theStoredStateIsKept() {
        service(90).open(user, null);

        verify(userRepository).save(any(User.class));
    }
}
