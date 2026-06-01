package com.example.worldcup.admin;

import com.example.worldcup.admin.dto.AdminConfigResponse;
import com.example.worldcup.user.Role;
import com.example.worldcup.user.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes a read-only view of effective server configuration to the admin
 * "Configurations" screen. Lives under {@code /api/admin/**} so it's
 * ADMIN-only. No secrets are returned — the API key appears as a boolean only.
 */
@RestController
@RequestMapping("/api/admin/config")
public class AdminConfigController {

    private final UserRepository userRepository;
    private final AdminProperties adminProperties;
    private final String apiFootballKey;

    public AdminConfigController(UserRepository userRepository,
                                 AdminProperties adminProperties,
                                 @Value("${app.api-football.key:}") String apiFootballKey) {
        this.userRepository = userRepository;
        this.adminProperties = adminProperties;
        this.apiFootballKey = apiFootballKey;
    }

    @GetMapping
    public AdminConfigResponse config() {
        return new AdminConfigResponse(
                adminProperties.hasName() ? adminProperties.name() : null,
                userRepository.count(),
                userRepository.countByRole(Role.ADMIN),
                isSet(apiFootballKey)
        );
    }

    private boolean isSet(String value) {
        return value != null && !value.isBlank();
    }
}
