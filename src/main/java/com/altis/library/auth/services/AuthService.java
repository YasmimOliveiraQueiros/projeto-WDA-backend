package com.altis.library.auth.services;

import com.altis.library.auth.models.dtos.LoginResponse;
import com.nimbusds.jose.JOSEException;
import com.altis.library.users.models.entities.User;
import com.altis.library.users.repositories.UserRepository;
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
            throw new RuntimeException("User not found");
        }

        if (!user.isActive()) {
            throw new RuntimeException("User is inactive");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
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
}
