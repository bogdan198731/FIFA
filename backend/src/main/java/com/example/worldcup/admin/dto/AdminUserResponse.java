package com.example.worldcup.admin.dto;

import com.example.worldcup.user.Role;
import com.example.worldcup.user.User;

import java.time.Instant;

/**
 * User row for the admin user-management screen.
 *
 * @param bootstrapAdmin true for the protected default admin (the one named by
 *                       {@code ADMIN_NAME}) — the UI uses this to lock its
 *                       controls, mirroring the server-side guard.
 */
public record AdminUserResponse(
        Long id,
        String username,
        String email,
        Role role,
        long totalPoints,
        Instant createdAt,
        boolean bootstrapAdmin
) {

    public static AdminUserResponse from(User user, boolean bootstrapAdmin) {
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getTotalPoints(),
                user.getCreatedAt(),
                bootstrapAdmin
        );
    }
}
