package com.luma.lumacourses.config;

import com.luma.lumacourses.common.enums.Role;
import com.luma.lumacourses.entity.User;
import com.luma.lumacourses.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final String ADMIN_EMAIL = "admin@luma.com";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_FULL_NAME = "Admin Luma";
    private static final String ADMIN_PASSWORD = "admin123";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.findByEmail(ADMIN_EMAIL).isPresent()) {
            log.info("Seed admin user already exists: {}", ADMIN_EMAIL);
            return;
        }

        User admin = new User();
        admin.setUsername(ADMIN_USERNAME);
        admin.setEmail(ADMIN_EMAIL);
        admin.setFullName(ADMIN_FULL_NAME);
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        admin.setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));

        userRepository.save(admin);
        log.info("Seeded admin user: {} / {}", ADMIN_EMAIL, ADMIN_PASSWORD);
    }
}
