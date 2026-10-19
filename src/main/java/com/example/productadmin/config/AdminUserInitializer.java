
package com.example.productadmin.config;

import com.example.productadmin.entity.User;
import com.example.productadmin.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminUserInitializer {

    @Bean
    public CommandLineRunner createAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            User admin = userRepository.findByEmail("admin@example.com")
                    .orElseGet(User::new);

            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("Admin@12345"));
            admin.setRole("ADMIN");

            userRepository.save(admin);

            System.out.println("Admin account created or updated!");
        };
    }
}

