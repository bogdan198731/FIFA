package com.example.worldcup.admin;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Bound to {@code app.admin.*}. Supplies the default ("bootstrap") admin
 * account created on startup from the {@code ADMIN_NAME} / {@code ADMIN_PASSWORD}
 * environment variables.
 *
 * @param name     the bootstrap admin's username. This account is protected —
 *                 it can never be demoted from ADMIN through the user-management
 *                 endpoints, so there's always at least one admin who can grant
 *                 rights to others.
 * @param password the bootstrap admin's password. Hashed with BCrypt before
 *                 storage. If blank, an already-existing account with {@code
 *                 name} is still ensured to have the ADMIN role, but no account
 *                 is created and no password is changed.
 */
@ConfigurationProperties("app.admin")
public record AdminProperties(String name, String password) {

    public boolean hasName() {
        return name != null && !name.isBlank();
    }

    public boolean hasPassword() {
        return password != null && !password.isBlank();
    }
}
