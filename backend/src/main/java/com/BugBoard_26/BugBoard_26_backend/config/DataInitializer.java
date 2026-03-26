package com.BugBoard_26.BugBoard_26_backend.config;

import com.BugBoard_26.BugBoard_26_backend.model.Role;
import com.BugBoard_26.BugBoard_26_backend.model.User;
import com.BugBoard_26.BugBoard_26_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.default.admin.password:admin-default-pass}")
    private String adminPassword;

    @Value("${app.default.dev.password:dev-default-pass}")
    private String devPassword;

    @Override
    public void run(String... args) throws Exception {
        // Se non c'è l'utente admin, crealo
        if (userRepository.findByEmail("admin@bugboard.com").isEmpty()) {
            User admin = new User();
            admin.setName("Admin");
            admin.setSurname("User");
            admin.setEmail("admin@bugboard.com");
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            System.out.println("Utente ADMIN creato: admin@bugboard.com");
        }

        // Se non c'è l'utente standard, crealo
        if (userRepository.findByEmail("dev@bugboard.com").isEmpty()) {
            User dev = new User();
            dev.setName("Developer");
            dev.setSurname("Mario");
            dev.setEmail("dev@bugboard.com");
            dev.setPassword(passwordEncoder.encode(devPassword));
            dev.setRole(Role.STANDARD);
            userRepository.save(dev);
            System.out.println("Utente STANDARD creato: dev@bugboard.com");
        }
    }
}
