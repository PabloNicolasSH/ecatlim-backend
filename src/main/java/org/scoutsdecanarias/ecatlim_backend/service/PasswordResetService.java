package org.scoutsdecanarias.ecatlim_backend.service;

import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.auth.password.ResetPasswordDto;
import org.scoutsdecanarias.ecatlim_backend.entity.User;
import org.scoutsdecanarias.ecatlim_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    @Value("${ecatlim.reset.link}")
    private String webPageLink;

    private static final String CACHE_NAME = "passwordResetCache";

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
        String resetLink = webPageLink + "/reset-password?token=" + token;
        emailService.sendRecoverPasswordEmail(email, resetLink);
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
}
