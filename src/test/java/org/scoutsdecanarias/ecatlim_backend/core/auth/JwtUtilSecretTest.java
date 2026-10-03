package org.scoutsdecanarias.ecatlim_backend.core.auth;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilSecretTest {

    private static JwtUtil withSecret(String secret) {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
        return jwtUtil;
    }

    @Test
    void acceptsA256BitBase64Secret() {
        String secret = Base64.getEncoder().encodeToString(new byte[32]);
        assertThatCode(() -> withSecret(secret).validateSecret()).doesNotThrowAnyException();
    }

    @Test
    void rejectsShortSecrets() {
        String secret = Base64.getEncoder().encodeToString(new byte[16]);
        assertThatThrownBy(() -> withSecret(secret).validateSecret())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("256 bits");
    }

    @Test
    void rejectsNonBase64Secrets() {
        assertThatThrownBy(() -> withSecret("not base64 !!").validateSecret())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Base64");
    }

    @Test
    void rejectsTheSecretThatWasCommittedToTheRepository() {
        String leaked = "MjJkY2QwZjhkZTkwMGRlNDhkNjdmY2RhZmI3NDVmYjZkN2U2ODFiMmE0YzAyN2M0NTliZWNjMTVkY2JjNjcxMw==";
        assertThatThrownBy(() -> withSecret(leaked).validateSecret())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("compromised");
    }
}
