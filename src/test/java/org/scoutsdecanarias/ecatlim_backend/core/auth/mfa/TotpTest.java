package org.scoutsdecanarias.ecatlim_backend.core.auth.mfa;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class TotpTest {

    /** The ASCII secret "12345678901234567890" of the RFC 6238 test vectors, in base32. */
    private static final String RFC_SECRET = Totp.base32Encode("12345678901234567890".getBytes(StandardCharsets.US_ASCII));

    @Test
    void matchesTheRfc6238TestVectors() {
        assertThat(Totp.codeAt(RFC_SECRET, 59 / 30)).isEqualTo("287082");
        assertThat(Totp.codeAt(RFC_SECRET, 1111111109L / 30)).isEqualTo("081804");
        assertThat(Totp.codeAt(RFC_SECRET, 1234567890L / 30)).isEqualTo("005924");
    }

    @Test
    void base32RoundTrips() {
        byte[] data = {1, 2, 3, 4, 5, 6, 7, (byte) 0xFF, (byte) 0x80, 42};

        assertThat(Totp.base32Decode(Totp.base32Encode(data))).isEqualTo(data);
        assertThat(RFC_SECRET).isEqualTo("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ");
    }

    @Test
    void acceptsOneStepOfClockDriftButNoMore() {
        long now = 1_700_000_000_000L;
        long step = Totp.stepAt(now);

        assertThat(Totp.matchingStep(RFC_SECRET, Totp.codeAt(RFC_SECRET, step), now)).isEqualTo(step);
        assertThat(Totp.matchingStep(RFC_SECRET, Totp.codeAt(RFC_SECRET, step - 1), now)).isEqualTo(step - 1);
        assertThat(Totp.matchingStep(RFC_SECRET, Totp.codeAt(RFC_SECRET, step + 1), now)).isEqualTo(step + 1);
        assertThat(Totp.matchingStep(RFC_SECRET, Totp.codeAt(RFC_SECRET, step - 5), now)).isEqualTo(-1);
    }

    @Test
    void rejectsMalformedCodes() {
        long now = System.currentTimeMillis();

        assertThat(Totp.matchingStep(RFC_SECRET, null, now)).isEqualTo(-1);
        assertThat(Totp.matchingStep(RFC_SECRET, "12345", now)).isEqualTo(-1);
        assertThat(Totp.matchingStep(RFC_SECRET, "abcdef", now)).isEqualTo(-1);
    }

    @Test
    void generatedSecretsAreValidAndDifferent() {
        String a = Totp.generateSecret();
        String b = Totp.generateSecret();

        assertThat(a).hasSize(32).matches("[A-Z2-7]+").isNotEqualTo(b);
    }

    @Test
    void buildsTheUriAuthenticatorAppsScan() {
        String uri = Totp.otpauthUri("ECATLIM", "ana@test.com", RFC_SECRET);

        assertThat(uri).startsWith("otpauth://totp/ECATLIM:ana%40test.com?secret=" + RFC_SECRET)
                .contains("issuer=ECATLIM", "digits=6", "period=30");
    }
}
