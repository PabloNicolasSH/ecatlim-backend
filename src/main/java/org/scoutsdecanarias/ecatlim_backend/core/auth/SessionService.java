package org.scoutsdecanarias.ecatlim_backend.core.auth;

import lombok.RequiredArgsConstructor;
import org.scoutsdecanarias.ecatlim_backend.core.UserDetailsServiceImpl;
import org.scoutsdecanarias.ecatlim_backend.core.auth.refresh.RefreshTokenService;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.UserDto;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

/** Opens a session once every login step is done: an access token and a refresh token. */
@Service
@RequiredArgsConstructor
public class SessionService {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsServiceImpl userDetailsService;

    public AuthResponse open(User user, List<String> recoveryCodes) {
        var userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtUtil.generateToken(userDetails, user.isMustChangePassword());
        String refreshToken = refreshTokenService.issue(user);
        return AuthResponse.authenticated(accessToken, refreshToken, user.isMustChangePassword(), recoveryCodes, UserDto.fromEntity(user));
    }
}
