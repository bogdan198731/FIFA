package com.example.worldcup.admin;

import com.example.worldcup.user.Role;
import com.example.worldcup.user.User;
import com.example.worldcup.user.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ensures the default ("bootstrap") admin account exists on every startup,
 * driven by {@code ADMIN_NAME} / {@code ADMIN_PASSWORD}.
 *
 * <p>Behaviour:
 * <ul>
 *   <li>If {@code ADMIN_NAME} is blank — nothing happens (logged once).</li>
 *   <li>If a user with that name already exists — it's promoted to ADMIN if it
 *       wasn't already, and its password is reset only when {@code ADMIN_PASSWORD}
 *       is provided.</li>
 *   <li>If no such user exists — it's created with the ADMIN role, but only
 *       when {@code ADMIN_PASSWORD} is provided (we won't create a passwordless
 *       account).</li>
 * </ul>
 *
 * <p>Runs in every profile (unlike {@link com.example.worldcup.auth.DevUserSeeder},
 * which is dev-only). Ordered after Flyway so the schema is in place.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;

    public AdminBootstrap(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          AdminProperties adminProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminProperties = adminProperties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!adminProperties.hasName()) {
            log.info("ADMIN_NAME not set — skipping bootstrap admin provisioning.");
            return;
        }
        String name = adminProperties.name().trim();

        userRepository.findByUsername(name).ifPresentOrElse(
                existing -> ensureExistingAdmin(existing),
                () -> createBootstrapAdmin(name)
        );
    }

    private void ensureExistingAdmin(User user) {
        boolean changed = false;
        if (user.getRole() != Role.ADMIN) {
            user.setRole(Role.ADMIN);
            changed = true;
            log.info("Promoted existing user '{}' to ADMIN (bootstrap admin).", user.getUsername());
        }
        if (adminProperties.hasPassword()) {
            user.setPassword(passwordEncoder.encode(adminProperties.password()));
            changed = true;
            log.info("Reset bootstrap admin '{}' password from ADMIN_PASSWORD.", user.getUsername());
        }
        if (changed) {
            userRepository.save(user);
        }
    }

    private void createBootstrapAdmin(String name) {
        if (!adminProperties.hasPassword()) {
            log.warn("Bootstrap admin '{}' does not exist and ADMIN_PASSWORD is not set — "
                    + "no admin was created. Set ADMIN_PASSWORD to provision it.", name);
            return;
        }
        String email = name.toLowerCase() + "@worldcup.local";
        User admin = new User(
                name,
                email,
                passwordEncoder.encode(adminProperties.password()),
                Role.ADMIN
        );
        userRepository.save(admin);
        log.info("Created bootstrap admin '{}'.", name);
    }
}
