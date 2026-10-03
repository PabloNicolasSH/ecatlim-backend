package org.scoutsdecanarias.ecatlim_backend.core.auth.password;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/password")
public class PasswordRequestController {

    private final PasswordResetService passwordResetService;

    public PasswordRequestController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @GetMapping("/forgot")
    public void forgotPassword(@RequestParam
                               @NotBlank(message = "El email es obligatorio")
                               @Email(message = "El email no tiene un formato válido")
                               @Size(max = 255, message = "El email no puede superar los 255 caracteres")
                               String email) {
        log.info("METHOD forgotPassword() - Forgot password request");
        passwordResetService.generatePasswordResetToken(email);
    }

    @PostMapping("/reset")
    public void resetPassword(@Valid @RequestBody ResetPasswordDto passwordDto) {
        log.info("METHOD resetPassword()");
        passwordResetService.resetPassword(passwordDto);
    }

    @PostMapping("/change-password")
    public void changePassword(@Valid @RequestBody ChangePasswordDto changePasswordDto) {
        log.info("METHOD changePassword() - Changing password of: {}", SecurityContextHolder.getContext().getAuthentication().getName());
        passwordResetService.changePassword(changePasswordDto);
    }
}
