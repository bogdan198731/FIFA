package com.example.worldcup.auth.dto;

public record AuthResponse(String token, long expiresInMs, UserResponse user) {
}
