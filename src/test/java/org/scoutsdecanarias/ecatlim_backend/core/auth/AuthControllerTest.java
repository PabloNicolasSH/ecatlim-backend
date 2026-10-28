package org.scoutsdecanarias.ecatlim_backend.core.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoutsdecanarias.ecatlim_backend.core.auth.mfa.MfaService;
import org.scoutsdecanarias.ecatlim_backend.core.auth.refresh.RefreshTokenService;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.enums.Role;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock UserRepository userRepository;
    @Mock SessionService sessionService;
    @Mock RefreshTokenService refreshTokenService;
    @Mock MfaService mfaService;

    private JwtUtil jwtUtil;
    private AuthController controller;
    private User user;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", Base64.getEncoder().encodeToString(new byte[32]));
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 60_000L);
        controller = new AuthController(authenticationManager, jwtUtil, userRepository, sessionService, refreshTokenService, mfaService);

        user = new User();
        user.setId(7);
        user.setEmail("user@test.com");
        lenient().when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
    }

    private AuthRequest request() {
        AuthRequest request = new AuthRequest();
        request.setUsername("user@test.com");
        request.setPassword("secret");
        return request;
    }

    @Test
    void aStudentGetsTheSessionRightAfterThePassword() {
        user.getRoles().add(Role.STUDENT);
        AuthResponse session = AuthResponse.authenticated("access", "refresh", false, null, null);
        when(sessionService.open(user, null)).thenReturn(session);

        assertThat(controller.login(request()).getBody()).isSameAs(session);
    }

    @Test
    void sensitiveRolesGetAChallengeInsteadOfTokens() {
        user.getRoles().add(Role.ADMIN);
        user.setTotpEnabled(true);

        AuthResponse response = controller.login(request()).getBody();

        assertThat(response.status()).isEqualTo(AuthResponse.Status.MFA_REQUIRED);
        assertThat(response.token()).isNull();
        assertThat(response.refreshToken()).isNull();
        assertThat(jwtUtil.extractChallengeUsername(response.challengeToken())).isEqualTo("user@test.com");
        verifyNoInteractions(sessionService);
    }

    @Test
    void sensitiveRolesWithoutASecondFactorMustSetItUp() {
        user.getRoles().add(Role.MANAGEMENT);

        AuthResponse response = controller.login(request()).getBody();

        assertThat(response.status()).isEqualTo(AuthResponse.Status.MFA_SETUP_REQUIRED);
        assertThat(response.challengeToken()).isNotBlank();
    }

    @Test
    void aWrongPasswordIsRejected() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> controller.login(request()))
                .isInstanceOf(EcatlimException.class)
                .extracting("status").isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void theSecondFactorStepOpensTheSession() {
        user.getRoles().add(Role.ADMIN);
        user.setTotpEnabled(true);
        String challenge = jwtUtil.generateChallengeToken("user@test.com");
        AuthResponse session = AuthResponse.authenticated("access", "refresh", false, null, null);
        when(sessionService.open(user, null)).thenReturn(session);

        var response = controller.verifyMfa(new AuthController.MfaCodeRequest(challenge, "123456"));

        verify(mfaService).verifyLogin(user, "123456");
        assertThat(response.getBody()).isSameAs(session);
    }

    @Test
    void anAccessTokenCannotBeUsedAsAChallenge() {
        var userDetails = new org.springframework.security.core.userdetails.User("user@test.com", "hash", java.util.List.of());
        String accessToken = jwtUtil.generateToken(userDetails);

        assertThatThrownBy(() -> controller.verifyMfa(new AuthController.MfaCodeRequest(accessToken, "123456")))
                .isInstanceOf(EcatlimException.class)
                .extracting("status").isEqualTo(HttpStatus.UNAUTHORIZED);
        verifyNoInteractions(mfaService);
    }

    @Test
    void aChallengeIsNotAValidAccessToken() {
        var userDetails = new org.springframework.security.core.userdetails.User("user@test.com", "hash", java.util.List.of());
        String challenge = jwtUtil.generateChallengeToken("user@test.com");

        assertThat(jwtUtil.validateToken(challenge, userDetails)).isFalse();
    }

    @Test
    void refreshingForcesANewLoginWhenTheSecondFactorWasReset() {
        user.getRoles().add(Role.HEAD_OF_EDUCATION);
        user.setTotpEnabled(false);
        when(refreshTokenService.consume("tok")).thenReturn(user);

        assertThatThrownBy(() -> controller.refresh(new AuthController.RefreshRequest("tok")))
                .isInstanceOf(EcatlimException.class)
                .extracting("status").isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(refreshTokenService).revokeAll(7);
    }

    @Test
    void refreshingRotatesTheTokens() {
        user.getRoles().add(Role.STUDENT);
        when(refreshTokenService.consume("tok")).thenReturn(user);
        AuthResponse session = AuthResponse.authenticated("access2", "refresh2", false, null, null);
        when(sessionService.open(user, null)).thenReturn(session);

        assertThat(controller.refresh(new AuthController.RefreshRequest("tok")).getBody()).isSameAs(session);
    }

    @Test
    void loggingOutRevokesTheRefreshToken() {
        controller.logout(new AuthController.RefreshRequest("tok"));

        verify(refreshTokenService).revoke("tok");
    }
}
