package com.example.worldcup.auth.dto;

import com.example.worldcup.user.Role;
import com.example.worldcup.user.User;

public record UserResponse(Long id, String username, String email, Role role) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }
}
