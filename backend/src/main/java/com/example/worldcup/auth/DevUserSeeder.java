package com.example.worldcup.auth;

import com.example.worldcup.user.User;
import com.example.worldcup.user.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Replaces the placeholder bcrypt strings inserted by the seed migration with a
 * real bcrypt hash so the seeded accounts can actually authenticate in dev.
 * Only runs under the {@code dev} profile.
 */
@Component
@Profile("dev")
public class DevUserSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevUserSeeder.class);
    private static final String DEV_PASSWORD = "password";
    private static final List<String> SEED_USERNAMES = List.of("admin", "alice", "bob");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DevUserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        String encoded = passwordEncoder.encode(DEV_PASSWORD);

        SEED_USERNAMES.forEach(username -> userRepository.findByUsername(username).ifPresent(user -> {
            if (!isBcrypt(user.getPassword())) {
                user.setPassword(encoded);
                userRepository.save(user);
                log.info("Reset password for seed user '{}' to the dev default", username);
            }
        }));
    }

    private boolean isBcrypt(String value) {
        return value != null && value.length() == 60
                && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }
}
