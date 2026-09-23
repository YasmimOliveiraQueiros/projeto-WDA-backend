package com.altis.library.config;

import com.altis.library.users.models.entities.User;
import com.altis.library.users.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminName;
    private final String adminEmail;
    private final String adminPassword;
    private final String adminPhone;
    private final String adminCpf;
    private final String adminBirthDate;
    private final String adminAddress;

    public AdminInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${admin.name}") String adminName,
            @Value("${admin.email}") String adminEmail,
            @Value("${admin.password}") String adminPassword,
            @Value("${admin.phone}") String adminPhone,
            @Value("${admin.cpf}") String adminCpf,
            @Value("${admin.birth-date}") String adminBirthDate,
            @Value("${admin.address}") String adminAddress) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminName = adminName;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminPhone = adminPhone;
        this.adminCpf = adminCpf;
        this.adminBirthDate = adminBirthDate;
        this.adminAddress = adminAddress;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByIsAdminTrue()) {
            return;
        }

        User admin = new User(
                adminName,
                adminEmail,
                null,
                adminPhone,
                adminCpf,
                LocalDate.parse(adminBirthDate),
                adminAddress
        );
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setAdmin(true);
        admin.setActive(true);

        userRepository.save(admin);
    }
}
