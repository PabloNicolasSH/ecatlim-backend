package org.scoutsdecanarias.ecatlim_backend.core.auth.refresh;

import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Slf4j
@Service
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long REUSE_GRACE_SECONDS = 15;
    private static final String INVALID = "La sesión no es válida, vuelva a iniciar sesión";

    private final RefreshTokenRepository repository;
    private final long refreshExpirationMs;

    public RefreshTokenService(RefreshTokenRepository repository,
                               @Value("${jwt.refreshExpirationMs:604800000}") long refreshExpirationMs) {
        this.repository = repository;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    /** Starts a session and returns the token the client must keep; it cannot be recovered later. */
    @Transactional
    public String issue(User user) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        LocalDateTime now = LocalDateTime.now();
        RefreshToken entity = new RefreshToken();
        entity.setUser(user);
        entity.setTokenHash(hash(token));
        entity.setCreatedAt(now);
        entity.setExpiresAt(now.plusNanos(refreshExpirationMs * 1_000_000));
        repository.save(entity);
        return token;
    }

    /**
     * Consumes a refresh token. Every token works once: a token that was already used means someone kept a copy,
     * so every session of that user is closed.
     */
    @Transactional(noRollbackFor = EcatlimException.class)
    public User consume(String token) {
        RefreshToken stored = token == null ? null : repository.findByTokenHash(hash(token)).orElse(null);
        if (stored == null) {
            throw new EcatlimException(INVALID, HttpStatus.UNAUTHORIZED);
        }
        LocalDateTime now = LocalDateTime.now();
        if (stored.getRevokedAt() != null) {
            if (stored.getRevokedAt().isAfter(now.minusSeconds(REUSE_GRACE_SECONDS))) {
                // Two tabs refreshing at the same moment: the loser must retry with the token the winner stored
                throw new EcatlimException(INVALID, HttpStatus.UNAUTHORIZED);
            }
            log.warn("METHOD consume() - Reused refresh token of user {}, revoking all their sessions", stored.getUser().getId());
            repository.revokeAllForUser(stored.getUser().getId(), now);
            throw new EcatlimException(INVALID, HttpStatus.UNAUTHORIZED);
        }
        if (stored.getExpiresAt().isBefore(now)) {
            throw new EcatlimException(INVALID, HttpStatus.UNAUTHORIZED);
        }
        User user = stored.getUser();
        if (!user.isEnabled()) {
            throw new EcatlimException(INVALID, HttpStatus.UNAUTHORIZED);
        }
        stored.setRevokedAt(now);
        repository.save(stored);
        return user;
    }

    @Transactional
    public void revoke(String token) {
        if (token == null) {
            return;
        }
        repository.findByTokenHash(hash(token)).ifPresent(stored -> {
            if (stored.getRevokedAt() == null) {
                stored.setRevokedAt(LocalDateTime.now());
                repository.save(stored);
            }
        });
    }

    @Transactional
    public void revokeAll(Integer userId) {
        repository.revokeAllForUser(userId, LocalDateTime.now());
    }

    @Scheduled(cron = "0 30 3 * * *", zone = "Atlantic/Canary")
    @Transactional
    public void deleteExpired() {
        int deleted = repository.deleteExpiredBefore(LocalDateTime.now().minusDays(1));
        log.info("METHOD deleteExpired() - {} expired refresh tokens removed", deleted);
    }

    static String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
