package com.example.worldcup.admin;

import com.example.worldcup.user.Role;
import com.example.worldcup.user.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full application-context boot test against an in-memory H2 database.
 *
 * <p>Proves the admin feature is actually usable end-to-end at runtime, not
 * just at compile time:
 * <ul>
 *   <li>the whole Spring context starts — every admin controller bean
 *       ({@code AdminUserController}, {@code AdminConfigController}, plus the
 *       existing match/score controllers) wires up alongside the security
 *       chain and {@code AdminProperties};</li>
 *   <li>{@link AdminBootstrap} provisions the default admin from the
 *       {@code app.admin.*} properties on startup.</li>
 * </ul>
 *
 * <p>Flyway is disabled and Hibernate creates the schema, because the real
 * migrations are PostgreSQL-specific.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:admintest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.flyway.enabled=false",
        "app.jwt.secret=test-secret-that-is-definitely-at-least-32-bytes-long",
        "app.admin.name=root",
        "app.admin.password=root-password",
        // Pin to empty so the test is hermetic — otherwise application.yml's
        // ${API_FOOTBALL_KEY:} resolves from the ambient shell environment.
        "app.api-football.key="
})
class AdminControllersContextTest {

    @Autowired AdminUserController adminUserController;
    @Autowired AdminConfigController adminConfigController;
    @Autowired AdminUserService adminUserService;
    @Autowired AdminProperties adminProperties;
    @Autowired UserRepository userRepository;

    @Test
    void adminControllerBeansAreWired() {
        assertThat(adminUserController).isNotNull();
        assertThat(adminConfigController).isNotNull();
        assertThat(adminUserService).isNotNull();
    }

    @Test
    void adminPropertiesBindFromConfig() {
        assertThat(adminProperties.name()).isEqualTo("root");
        assertThat(adminProperties.hasPassword()).isTrue();
    }

    @Test
    void bootstrapAdminIsProvisionedOnStartup() {
        var admin = userRepository.findByUsername("root");
        assertThat(admin).isPresent();
        assertThat(admin.get().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void configEndpointReportsTheBootstrapAdmin() {
        var config = adminConfigController.config();
        assertThat(config.bootstrapAdminName()).isEqualTo("root");
        assertThat(config.adminCount()).isGreaterThanOrEqualTo(1);
        assertThat(config.apiFootballKeyConfigured()).isFalse();
    }
}
