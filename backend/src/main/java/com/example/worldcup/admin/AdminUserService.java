package com.example.worldcup.admin;

import com.example.worldcup.admin.dto.AdminUserResponse;
import com.example.worldcup.common.ApiException;
import com.example.worldcup.user.Role;
import com.example.worldcup.user.User;
import com.example.worldcup.user.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * User administration: listing users and granting/revoking the ADMIN role.
 *
 * <p>Two guards keep an admin from accidentally locking the system out of
 * administration:
 * <ol>
 *   <li>An admin can't change their <em>own</em> role (no self-demotion).</li>
 *   <li>The bootstrap admin (named by {@code ADMIN_NAME}) can never be
 *       demoted — there's always at least one admin able to grant rights.</li>
 * </ol>
 */
@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final AdminProperties adminProperties;

    public AdminUserService(UserRepository userRepository, AdminProperties adminProperties) {
        this.userRepository = userRepository;
        this.adminProperties = adminProperties;
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> listUsers() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getUsername, String.CASE_INSENSITIVE_ORDER))
                .map(u -> AdminUserResponse.from(u, isBootstrapAdmin(u)))
                .toList();
    }

    @Transactional
    public AdminUserResponse updateRole(Long actingAdminId,
                                        Long targetUserId,
                                        Role newRole) {
        if (Objects.equals(actingAdminId, targetUserId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "You cannot change your own role.");
        }

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        if (isBootstrapAdmin(target) && newRole != Role.ADMIN) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "The default admin's role cannot be changed.");
        }

        target.setRole(newRole);
        return AdminUserResponse.from(target, isBootstrapAdmin(target));
    }

    private boolean isBootstrapAdmin(User user) {
        return adminProperties.hasName()
                && user.getUsername().equalsIgnoreCase(adminProperties.name().trim());
    }
}
