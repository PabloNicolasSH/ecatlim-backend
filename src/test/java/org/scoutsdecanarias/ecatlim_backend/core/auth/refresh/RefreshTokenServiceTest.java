package org.scoutsdecanarias.ecatlim_backend.core.auth.refresh;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock RefreshTokenRepository repository;

    private RefreshTokenService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenService(repository, 7 * 24 * 3600 * 1000L);
        user = new User();
        user.setId(7);
        user.setEmail("user@test.com");
    }

    private RefreshToken stored(LocalDateTime expiresAt, LocalDateTime revokedAt) {
        RefreshToken stored = new RefreshToken();
        stored.setUser(user);
        stored.setExpiresAt(expiresAt);
        stored.setRevokedAt(revokedAt);
        return stored;
    }

    @Test
    void storesOnlyTheHashOfTheIssuedToken() {
        String token = service.issue(user);

        ArgumentCaptor<RefreshToken> saved = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getTokenHash()).isEqualTo(RefreshTokenService.hash(token)).isNotEqualTo(token).hasSize(64);
        assertThat(saved.getValue().getExpiresAt()).isAfter(LocalDateTime.now().plusDays(6));
    }

    @Test
    void consumingATokenRevokesItSoItWorksOnlyOnce() {
        RefreshToken stored = stored(LocalDateTime.now().plusDays(1), null);
        when(repository.findByTokenHash(RefreshTokenService.hash("tok"))).thenReturn(Optional.of(stored));

        assertThat(service.consume("tok")).isSameAs(user);

        assertThat(stored.getRevokedAt()).isNotNull();
    }

    @Test
    void unknownExpiredAndDeactivatedAreRejected() {
        when(repository.findByTokenHash(RefreshTokenService.hash("unknown"))).thenReturn(Optional.empty());
        when(repository.findByTokenHash(RefreshTokenService.hash("expired"))).thenReturn(Optional.of(stored(LocalDateTime.now().minusMinutes(1), null)));
        user.setEnabled(false);
        when(repository.findByTokenHash(RefreshTokenService.hash("inactive"))).thenReturn(Optional.of(stored(LocalDateTime.now().plusDays(1), null)));

        for (String token : new String[]{"unknown", "expired", "inactive", null}) {
            assertThatThrownBy(() -> service.consume(token))
                    .isInstanceOf(EcatlimException.class)
                    .extracting("status").isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Test
    void reusingAnOldTokenClosesEverySessionOfTheUser() {
        RefreshToken used = stored(LocalDateTime.now().plusDays(1), LocalDateTime.now().minusMinutes(5));
        when(repository.findByTokenHash(RefreshTokenService.hash("old"))).thenReturn(Optional.of(used));

        assertThatThrownBy(() -> service.consume("old")).isInstanceOf(EcatlimException.class);

        verify(repository).revokeAllForUser(eq(7), any(LocalDateTime.class));
    }

    @Test
    void aTokenUsedAMomentAgoIsRejectedWithoutClosingTheSessions() {
        RefreshToken justUsed = stored(LocalDateTime.now().plusDays(1), LocalDateTime.now().minusSeconds(2));
        when(repository.findByTokenHash(RefreshTokenService.hash("race"))).thenReturn(Optional.of(justUsed));

        assertThatThrownBy(() -> service.consume("race")).isInstanceOf(EcatlimException.class);

        verify(repository, never()).revokeAllForUser(any(), any());
    }
}
