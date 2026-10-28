package org.scoutsdecanarias.ecatlim_backend.core.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.UserDto;

import java.util.List;

/**
 * Result of a login step. AUTHENTICATED carries the access and refresh tokens; the other statuses only carry a
 * challenge token that lets the client finish the second factor step.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        Status status,
        String token,
        String refreshToken,
        String challengeToken,
        boolean passwordChangeRequired,
        List<String> recoveryCodes,
        UserDto user
) {
    public enum Status {
        AUTHENTICATED,
        MFA_REQUIRED,
        MFA_SETUP_REQUIRED
    }

    public static AuthResponse authenticated(String token, String refreshToken, boolean passwordChangeRequired,
                                             List<String> recoveryCodes, UserDto user) {
        return new AuthResponse(Status.AUTHENTICATED, token, refreshToken, null, passwordChangeRequired, recoveryCodes, user);
    }

    public static AuthResponse challenge(Status status, String challengeToken) {
        return new AuthResponse(status, null, null, challengeToken, false, null, null);
    }
}
