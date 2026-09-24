package com.altis.library.auth.services;

import com.altis.library.auth.models.dtos.LoginResponse;
import com.altis.library.auth.models.dtos.PasswordRecoveryVerificationRequest;
import com.altis.library.auth.models.dtos.PasswordResetRequest;
import com.nimbusds.jose.JOSEException;
import com.altis.library.users.models.entities.User;
import com.altis.library.users.repositories.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse authenticateByEmail(String email, String password) {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new BadCredentialsException("E-mail ou senha inválidos.");
        }

        if (!user.isActive()) {
            throw new BadCredentialsException("E-mail ou senha inválidos.");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("E-mail ou senha inválidos.");
        }

        try {
            String role = user.isAdmin() ? "ADMIN" : "USER";
            return new LoginResponse(
                    jwtService.generateToken(user.getEmail(), user.isAdmin()),
                    role
            );
        } catch (JOSEException exception) {
            throw new IllegalStateException("Unable to generate authentication token", exception);
        }
    }

    public void verifyPasswordRecovery(PasswordRecoveryVerificationRequest request) {
        userRepository.findByEmailAndCpf(request.getEmail(), request.getCpf())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Não foi possível validar os dados de recuperação."
                ));
    }

    public void resetPassword(PasswordResetRequest request) {
        User user = userRepository.findByEmailAndCpf(request.getEmail(), request.getCpf())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Não foi possível validar os dados de recuperação."
                ));

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("As senhas não coincidem.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
