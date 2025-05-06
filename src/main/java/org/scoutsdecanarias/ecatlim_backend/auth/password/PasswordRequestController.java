package org.scoutsdecanarias.ecatlim_backend.auth.password;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.service.PasswordResetService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/password")
public class PasswordRequestController {

    private final PasswordResetService passwordResetService;

    public PasswordRequestController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @GetMapping("/forgot")
    public void forgotPassword(@NotNull @RequestParam String email) {
        log.info("METHOD forgotPassword - Forgot password request for email: {}", email);
        passwordResetService.generatePasswordResetToken(email);
    }

    @PostMapping("/reset")
    public void resetPassword(@Valid @RequestBody ResetPasswordDto passwordDto) {
        log.info("METHOD resetPassword()");
        passwordResetService.resetPassword(passwordDto);
    }

    @PostMapping("/change-password")
    public void changePassword(@RequestBody ChangePasswordDto changePasswordDto) {
        log.info("METHOD changePassword() - Changing password of: {}", SecurityContextHolder.getContext().getAuthentication().getName());
        passwordResetService.changePassword(changePasswordDto);
    }
}
