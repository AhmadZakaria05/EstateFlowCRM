
package com.EstateFlowCRM.platform.security.util;

import com.EstateFlowCRM.platform.user.entities.Role;
import com.EstateFlowCRM.platform.user.entities.User;
import com.EstateFlowCRM.platform.user.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap-admin.full-name:EstateFlow Admin}")
    private String adminFullName;

    @Value("${app.bootstrap-admin.email}")
    private String adminEmail;

    @Value("${app.bootstrap-admin.password}")
    private String adminPassword;

//    public AdminInitializer(
//            UserRepository userRepository,
//            PasswordEncoder passwordEncoder) {
//
//        this.userRepository = userRepository;
//        this.passwordEncoder = passwordEncoder;
//    }

    @Override
    public void run(ApplicationArguments args) {

        // Check if an Overview Admin already exists
        if (userRepository.existsByRole(Role.OVERVIEW_ADMIN)) {
            System.out.println(
                    "Overview Admin already exists. Skipping initialization."
            );
            return;
        }

        String normalizedEmail = adminEmail
                .trim()
                .toLowerCase(Locale.ROOT);

        // Prevent using an email already assigned to another role
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalStateException(
                    "Bootstrap admin email already belongs to another user."
            );
        }

        // Create initial admin
        User admin = new User();

        admin.setFullName(adminFullName);
        admin.setEmail(normalizedEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.OVERVIEW_ADMIN);
        admin.setEnabled(true);
        admin.setMustChangePassword(true);

        userRepository.save(admin);

        System.out.println(
                "Initial Overview Admin created successfully."
        );
    }
}
