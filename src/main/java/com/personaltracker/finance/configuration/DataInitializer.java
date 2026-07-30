package com.personaltracker.finance.configuration;

import com.personaltracker.finance.models.User;
import com.personaltracker.finance.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Database Initializer to seed default demo credentials on startup.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("dummy@gmail.com") == null) {
            User dummyUser = new User();
            dummyUser.setName("Demo User");
            dummyUser.setEmail("dummy@gmail.com");
            dummyUser.setPassword(passwordEncoder.encode("password"));
            dummyUser.setVerified(true);
            dummyUser.setCurrentBalance(new BigDecimal("50000.00"));

            userRepository.save(dummyUser);
            log.info("Successfully seeded demo user account: dummy@gmail.com");
        }
    }
}
