package org.scoutsdecanarias.ecatlim_backend.core.auth.password;

import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.core.auth.SecurityUtils;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimBadRequestException;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.scoutsdecanarias.ecatlim_backend.shared.email.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class PasswordResetService {

    private final PasswordEncoder passwordEncoder;
    private final CacheManager cacheManager;

    private final EmailService emailService;
    private final UserRepository userRepository;

    @Value("${ecatlim.link}")
    private String webPageLink;

    private static final String CACHE_NAME = "passwordResetCache";
    public static final String FAKE_PASSWORD = "fake_password";

    public PasswordResetService(PasswordEncoder passwordEncoder, EmailService emailService, CacheManager cacheManager, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.cacheManager = cacheManager;
        this.userRepository = userRepository;
    }

    public void generatePasswordResetToken(String email) {
        try {
            String token = UUID.randomUUID().toString();
            cacheManager.getCache(CACHE_NAME).put(token, email);
            sendPasswordResetEmail(email, token);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void sendPasswordResetEmail(String email, String token) {
        emailService.sendRecoverPasswordEmail(email, token);
    }

    public void resetPassword(ResetPasswordDto passwordDto) {
        Cache cache = cacheManager.getCache(CACHE_NAME);

        assert cache != null;
        Cache.ValueWrapper wrapper = cache.get(passwordDto.token());

        if (wrapper == null || wrapper.get() == null) {
            throw new RuntimeException("Token inválido o caducado");
        }

        String email = (String) wrapper.get();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        user.setPassword(passwordEncoder.encode(passwordDto.newPassword()));
        userRepository.save(user);

        cache.evict(passwordDto.token());
    }

    public void changePassword(ChangePasswordDto changePasswordDto) {
        User user = userRepository.findByEmail(SecurityUtils.getLoggedUsername()).orElseThrow(() -> new UsernameNotFoundException(SecurityUtils.getLoggedUsername()));

        if (!changePasswordDto.newPassword().equals(changePasswordDto.newPasswordRepeat())){
            log.warn("Passwords do not match");
            throw new EcatlimBadRequestException("Las contraseñas no coinciden");
        }

        if (changePasswordDto.newPassword().equals(FAKE_PASSWORD)) {
            log.warn("New password is not valid");
            throw new EcatlimBadRequestException("La nueva contraseña no es válida");
        }
        if (!BCrypt.checkpw(changePasswordDto.currentPassword(), user.getPassword())) {
            log.warn("Current password is not valid");
            throw new EcatlimBadRequestException("La contraseña actual no es válida");
        }
        user.setPassword(passwordEncoder.encode(changePasswordDto.newPassword()));
        userRepository.save(user);
    }
}
