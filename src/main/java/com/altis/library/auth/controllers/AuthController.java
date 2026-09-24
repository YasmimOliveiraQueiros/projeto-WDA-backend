package com.altis.library.auth.controllers;

import com.altis.library.auth.models.dtos.LoginRequest;
import com.altis.library.auth.models.dtos.LoginResponse;
import com.altis.library.auth.models.dtos.PasswordRecoveryVerificationRequest;
import com.altis.library.auth.models.dtos.PasswordResetRequest;
import com.altis.library.auth.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.authenticateByEmail(
                request.getEmail(),
                request.getPassword()
        );
    }

    @PostMapping("/password-recovery/verify")
    public void verifyPasswordRecovery(
            @Valid @RequestBody PasswordRecoveryVerificationRequest request) {
        authService.verifyPasswordRecovery(request);
    }

    @PostMapping("/password-recovery/reset")
    public void resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(request);
    }
}
