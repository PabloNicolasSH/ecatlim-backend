package org.scoutsdecanarias.ecatlim_backend.auth;

import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.entity.User;
import org.scoutsdecanarias.ecatlim_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getLoggedUser() {
        Optional<User> user = userRepository.findByEmail(SecurityUtils.getLoggedUsername());
        if (user.isEmpty()) {
            log.error("User not found");
        }
        return user.orElseThrow();
    }
}
