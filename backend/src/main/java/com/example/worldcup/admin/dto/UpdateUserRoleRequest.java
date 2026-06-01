package com.example.worldcup.admin.dto;

import com.example.worldcup.user.Role;

import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(@NotNull Role role) {
}
