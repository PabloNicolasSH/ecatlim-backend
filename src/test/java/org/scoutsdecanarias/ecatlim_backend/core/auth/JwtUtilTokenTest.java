package org.scoutsdecanarias.ecatlim_backend.core.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTokenTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", Base64.getEncoder().encodeToString(new byte[32]));
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 60_000L);
    }

    private UserDetails user(String password, boolean enabled) {
        return new User("user@test.com", password, enabled, true, true, true, List.of());
    }

    @Test
    void aFreshTokenIsValid() {
        String token = jwtUtil.generateToken(user("hash-1", true));

        assertThat(jwtUtil.validateToken(token, user("hash-1", true))).isTrue();
    }

    @Test
    void aTokenStopsWorkingWhenThePasswordChanges() {
        String token = jwtUtil.generateToken(user("hash-1", true));

        assertThat(jwtUtil.validateToken(token, user("hash-2", true))).isFalse();
    }

    @Test
    void aTokenStopsWorkingWhenTheUserIsDeactivated() {
        String token = jwtUtil.generateToken(user("hash-1", true));

        assertThat(jwtUtil.validateToken(token, user("hash-1", false))).isFalse();
    }
}
