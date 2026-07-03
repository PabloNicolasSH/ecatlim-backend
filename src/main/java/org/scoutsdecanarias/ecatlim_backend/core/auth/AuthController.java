package org.scoutsdecanarias.ecatlim_backend.core.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.UserProfileMinDto;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.enums.Role;
import org.scoutsdecanarias.ecatlim_backend.features.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        log.info("METHOD login() - Login request by: {}", request.getUsername());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String token = jwtUtil.generateToken((UserDetails) authentication.getPrincipal());
        User user = userRepository.findByEmail(request.getUsername()).orElse(null);

        UserProfileMinDto profileMinDto = null;

        assert user != null;

        if (user.getProfile() != null) {
            profileMinDto = new UserProfileMinDto(
                    user.getProfile().getName(),
                    user.getProfile().getSurname()
            );
        }

        return ResponseEntity.ok(new AuthResponse(token, user.getEmail(), user.getRole(), profileMinDto));
    }

    @Data
    @AllArgsConstructor
    static
    class AuthResponse {
        private String token;
        private String email;
        private Role role;
        private UserProfileMinDto profile;
    }
}
