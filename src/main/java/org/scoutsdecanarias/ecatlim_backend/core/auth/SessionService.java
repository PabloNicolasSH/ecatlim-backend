package org.scoutsdecanarias.ecatlim_backend.core.auth;

import org.scoutsdecanarias.ecatlim_backend.core.UserDetailsServiceImpl;
import org.scoutsdecanarias.ecatlim_backend.core.auth.refresh.RefreshTokenService;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.UserDto;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/** Opens a session once every login step is done: an access token and a refresh token. */
@Service
public class SessionService {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserRepository userRepository;
    private final int passwordMaxAgeDays;

    public SessionService(JwtUtil jwtUtil, RefreshTokenService refreshTokenService, UserDetailsServiceImpl userDetailsService,
                          UserRepository userRepository,
                          @Value("${ecatlim.password-max-age-days:90}") int passwordMaxAgeDays) {
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.passwordMaxAgeDays = passwordMaxAgeDays;
    }

    /** A new login: records it and opens the session. */
    public AuthResponse open(User user, List<String> recoveryCodes) {
        user.setLastLoginAt(LocalDateTime.now());
        return start(user, recoveryCodes);
    }

    /** Renewal of a session that was already open, which is not a login. */
    public AuthResponse renew(User user) {
        return start(user, null);
    }

    private AuthResponse start(User user, List<String> recoveryCodes) {
        if (isPasswordExpired(user)) {
            user.setMustChangePassword(true);
        }
        userRepository.save(user);

        var userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtUtil.generateToken(userDetails, user.isMustChangePassword());
        String refreshToken = refreshTokenService.issue(user);
        return AuthResponse.authenticated(accessToken, refreshToken, user.isMustChangePassword(), recoveryCodes, UserDto.fromEntity(user));
    }

    /** Passwords older than ecatlim.password-max-age-days must be changed; 0 turns the rule off. */
    private boolean isPasswordExpired(User user) {
        return passwordMaxAgeDays > 0 && user.getPasswordChangedAt() != null
                && user.getPasswordChangedAt().plusDays(passwordMaxAgeDays).isBefore(LocalDateTime.now());
    }
}
