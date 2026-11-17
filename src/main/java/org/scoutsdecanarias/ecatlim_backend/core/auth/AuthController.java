package org.scoutsdecanarias.ecatlim_backend.core.auth;

import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.core.auth.mfa.MfaService;
import org.scoutsdecanarias.ecatlim_backend.core.auth.refresh.RefreshTokenService;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    public record RefreshRequest(@NotBlank(message = "La sesión no es válida") String refreshToken) {}

    public record MfaChallengeRequest(@NotBlank(message = "La sesión no es válida") String challengeToken) {}

    public record MfaCodeRequest(@NotBlank(message = "La sesión no es válida") String challengeToken,
                                 @NotBlank(message = "El código es obligatorio") String code) {}

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final SessionService sessionService;
    private final RefreshTokenService refreshTokenService;
    private final MfaService mfaService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserRepository userRepository,
                          SessionService sessionService, RefreshTokenService refreshTokenService, MfaService mfaService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.sessionService = sessionService;
        this.refreshTokenService = refreshTokenService;
        this.mfaService = mfaService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        log.info("METHOD login() - Login request by: {}", request.getUsername());

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user {}: {}", request.getUsername(), e.getMessage());
            throw new EcatlimException("El usuario o la contraseña son incorrectos", HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findByEmail(request.getUsername()).orElseThrow(() -> new EcatlimException("Usuario no encontrado", HttpStatus.NOT_FOUND));

        if (MfaService.isRequiredFor(user)) {
            AuthResponse.Status status = user.isTotpEnabled() ? AuthResponse.Status.MFA_REQUIRED : AuthResponse.Status.MFA_SETUP_REQUIRED;
            return ResponseEntity.ok(AuthResponse.challenge(status, jwtUtil.generateChallengeToken(user.getEmail())));
        }
        return ResponseEntity.ok(sessionService.open(user, null));
    }

    @PostMapping("/mfa/verify")
    public ResponseEntity<AuthResponse> verifyMfa(@Valid @RequestBody MfaCodeRequest request) {
        User user = userFromChallenge(request.challengeToken());
        mfaService.verifyLogin(user, request.code());
        return ResponseEntity.ok(sessionService.open(user, null));
    }

    @PostMapping("/mfa/setup")
    public ResponseEntity<MfaService.Setup> beginMfaSetup(@Valid @RequestBody MfaChallengeRequest request) {
        return ResponseEntity.ok(mfaService.beginSetup(userFromChallenge(request.challengeToken())));
    }

    @PostMapping("/mfa/setup/confirm")
    public ResponseEntity<AuthResponse> confirmMfaSetup(@Valid @RequestBody MfaCodeRequest request) {
        User user = userFromChallenge(request.challengeToken());
        var recoveryCodes = mfaService.confirmSetup(user, request.code());
        return ResponseEntity.ok(sessionService.open(user, recoveryCodes));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        User user = refreshTokenService.consume(request.refreshToken());
        if (MfaService.isRequiredFor(user) && !user.isTotpEnabled()) {
            // The second factor was reset or the role was given after the login: they have to go through it again
            refreshTokenService.revokeAll(user.getId());
            throw new EcatlimException("La sesión no es válida, vuelva a iniciar sesión", HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok(sessionService.renew(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshRequest request) {
        refreshTokenService.revoke(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    private User userFromChallenge(String challengeToken) {
        try {
            String email = jwtUtil.extractChallengeUsername(challengeToken);
            return userRepository.findByEmail(email)
                    .filter(User::isEnabled)
                    .orElseThrow(() -> new EcatlimException("La sesión no es válida, vuelva a iniciar sesión", HttpStatus.UNAUTHORIZED));
        } catch (JwtException | IllegalArgumentException e) {
            throw new EcatlimException("El paso de verificación ha caducado, vuelve a iniciar sesión", HttpStatus.UNAUTHORIZED);
        }
    }
}
