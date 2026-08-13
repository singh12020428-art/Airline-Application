package com.zosh.service.Impl;

import com.zosh.enums.UserRole;
import com.zosh.model.User;
import com.zosh.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializationComponent implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeAdminUser();
    }

    private void initializeAdminUser() {
        String email = "codewithzosh@gmail.com";
        String password = "codewithzosh";

        if (userRepository.findByEmail(email) == null) {
            User adminUser = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .fullName("zosh")
                    .role(UserRole.ROLE_SYSTEM_ADMIN)
                    .build();

            userRepository.save(adminUser);
            System.out.println(" Admin user initialized: " + email);
        } else {
            System.out.println(" Admin user already exists: " + email);
        }
    }
}
