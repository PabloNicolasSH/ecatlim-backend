package org.scoutsdecanarias.ecatlim_backend.core.auth.mfa;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.enums.Role;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/** Two-factor authentication with an authenticator app (TOTP) and single-use recovery codes. */
@Slf4j
@Service
public class MfaService {

    /** Roles that cannot log in without a second factor. */
    public static final Set<Role> REQUIRED_ROLES = Set.of(
            Role.ADMIN, Role.HEAD_OF_EDUCATION, Role.MANAGER_DIRECTOR, Role.MANAGEMENT);

    private static final int RECOVERY_CODES = 8;
    private static final String RECOVERY_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    public record Setup(String secret, String otpauthUri) {}

    private final UserRepository userRepository;
    private final MfaRecoveryCodeRepository recoveryCodeRepository;
    private final SecretCipher cipher;
    private final String issuer;

    /** Failed codes per user in the last 10 minutes; reaching the limit locks the second factor for the rest of the window. */
    private final Cache<Integer, AtomicInteger> failedAttempts = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10))
            .maximumSize(10_000)
            .build();

    public MfaService(UserRepository userRepository, MfaRecoveryCodeRepository recoveryCodeRepository,
                      SecretCipher cipher, @Value("${ecatlim.mfa.issuer:ECATLIM}") String issuer) {
        this.userRepository = userRepository;
        this.recoveryCodeRepository = recoveryCodeRepository;
        this.cipher = cipher;
        this.issuer = issuer;
    }

    public static boolean isRequiredFor(User user) {
        return user.getRoles().stream().anyMatch(REQUIRED_ROLES::contains);
    }

    /** Creates a secret for the user to add to their app. It only counts once a code of it is confirmed. */
    @Transactional
    public Setup beginSetup(User user) {
        if (user.isTotpEnabled()) {
            throw new EcatlimException("La verificación en dos pasos ya está activada", HttpStatus.CONFLICT);
        }
        String secret = Totp.generateSecret();
        user.setTotpSecret(cipher.encrypt(secret));
        user.setTotpLastStep(null);
        userRepository.save(user);
        return new Setup(secret, Totp.otpauthUri(issuer, user.getEmail(), secret));
    }

    /** Activates the second factor after checking a first code, and returns the recovery codes, shown only once. */
    @Transactional(noRollbackFor = EcatlimException.class)
    public List<String> confirmSetup(User user, String code) {
        if (user.isTotpEnabled()) {
            throw new EcatlimException("La verificación en dos pasos ya está activada", HttpStatus.CONFLICT);
        }
        if (user.getTotpSecret() == null) {
            throw new EcatlimException("Primero hay que iniciar la configuración", HttpStatus.CONFLICT);
        }
        assertNotLocked(user);
        long step = Totp.matchingStep(cipher.decrypt(user.getTotpSecret()), clean(code), System.currentTimeMillis());
        if (step < 0) {
            throw wrongCode(user);
        }
        failedAttempts.invalidate(user.getId());

        user.setTotpEnabled(true);
        user.setTotpLastStep(step);
        userRepository.save(user);
        log.info("METHOD confirmSetup() - Two-factor authentication enabled for user {}", user.getId());
        return generateRecoveryCodes(user);
    }

    /** Checks the second factor of a login: a code of the app or one of the recovery codes. */
    @Transactional(noRollbackFor = EcatlimException.class)
    public void verifyLogin(User user, String code) {
        if (!user.isTotpEnabled() || user.getTotpSecret() == null) {
            throw new EcatlimException("La verificación en dos pasos no está configurada", HttpStatus.CONFLICT);
        }
        assertNotLocked(user);
        String cleaned = clean(code);

        if (cleaned.matches("\\d{6}")) {
            long step = Totp.matchingStep(cipher.decrypt(user.getTotpSecret()), cleaned, System.currentTimeMillis());
            boolean replayed = step >= 0 && user.getTotpLastStep() != null && step <= user.getTotpLastStep();
            if (step < 0 || replayed) {
                throw wrongCode(user);
            }
            user.setTotpLastStep(step);
            userRepository.save(user);
        } else {
            var recovery = recoveryCodeRepository.findByUserIdAndCodeHashAndUsedAtIsNull(user.getId(), hash(normalizeRecovery(cleaned)));
            if (recovery.isEmpty()) {
                throw wrongCode(user);
            }
            recovery.get().setUsedAt(LocalDateTime.now());
            recoveryCodeRepository.save(recovery.get());
            log.info("METHOD verifyLogin() - Recovery code used by user {}", user.getId());
        }
        failedAttempts.invalidate(user.getId());
    }

    public long remainingRecoveryCodes(User user) {
        return recoveryCodeRepository.countByUserIdAndUsedAtIsNull(user.getId());
    }

    /** For people who lost their device: they have to set it up again at their next login. */
    @Transactional
    public void reset(User user) {
        user.setTotpEnabled(false);
        user.setTotpSecret(null);
        user.setTotpLastStep(null);
        userRepository.save(user);
        recoveryCodeRepository.deleteAllForUser(user.getId());
        failedAttempts.invalidate(user.getId());
        log.info("METHOD reset() - Two-factor authentication reset for user {}", user.getId());
    }

    private List<String> generateRecoveryCodes(User user) {
        recoveryCodeRepository.deleteAllForUser(user.getId());
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < RECOVERY_CODES; i++) {
            StringBuilder code = new StringBuilder();
            for (int j = 0; j < 10; j++) {
                code.append(RECOVERY_ALPHABET.charAt(RANDOM.nextInt(RECOVERY_ALPHABET.length())));
            }
            MfaRecoveryCode entity = new MfaRecoveryCode();
            entity.setUser(user);
            entity.setCodeHash(hash(code.toString()));
            recoveryCodeRepository.save(entity);
            codes.add(code.substring(0, 5) + "-" + code.substring(5));
        }
        return codes;
    }

    private void assertNotLocked(User user) {
        AtomicInteger failed = failedAttempts.getIfPresent(user.getId());
        if (failed != null && failed.get() >= MAX_FAILED_ATTEMPTS) {
            throw new EcatlimException("Demasiados intentos fallidos, espera unos minutos", HttpStatus.TOO_MANY_REQUESTS);
        }
    }

    private EcatlimException wrongCode(User user) {
        failedAttempts.get(user.getId(), id -> new AtomicInteger()).incrementAndGet();
        return new EcatlimException("El código no es correcto", HttpStatus.UNAUTHORIZED);
    }

    private static String clean(String code) {
        return code == null ? "" : code.trim().replace(" ", "");
    }

    private static String normalizeRecovery(String code) {
        return code.replace("-", "").toUpperCase(Locale.ROOT);
    }

    private static String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
