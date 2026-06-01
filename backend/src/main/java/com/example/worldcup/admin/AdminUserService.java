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
 * <p>Three guards keep the system from ever losing administrative access:
 * <ol>
 *   <li><b>No self-demotion</b> — an admin can't change their own role, so the
 *       default admin can't revoke its own rights.</li>
 *   <li><b>Bootstrap admin is protected</b> — the account named by
 *       {@code ADMIN_NAME} can never be demoted by anyone.</li>
 *   <li><b>Last admin is protected</b> — demoting the only remaining admin is
 *       rejected. This is config-independent, so it holds even if
 *       {@code ADMIN_NAME} isn't set to match the default admin's username.</li>
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
        // Guard 1: an admin can never change their own role.
        if (Objects.equals(actingAdminId, targetUserId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "You cannot change your own role.");
        }

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        boolean isDemotion = target.getRole() == Role.ADMIN && newRole != Role.ADMIN;

        if (isDemotion) {
            // Guard 2: the configured default admin is never demotable.
            if (isBootstrapAdmin(target)) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "The default admin's role cannot be changed.");
            }
            // Guard 3: never demote the last remaining admin (config-independent).
            if (userRepository.countByRole(Role.ADMIN) <= 1) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Cannot remove the last administrator.");
            }
        }

        target.setRole(newRole);
        return AdminUserResponse.from(target, isBootstrapAdmin(target));
    }

    private boolean isBootstrapAdmin(User user) {
        return adminProperties.hasName()
                && user.getUsername().equalsIgnoreCase(adminProperties.name().trim());
    }
}
