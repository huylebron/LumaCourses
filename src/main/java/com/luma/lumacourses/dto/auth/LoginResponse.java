package com.luma.lumacourses.dto.auth;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,      // always "Bearer"
        long expiresIn,        // seconds until access token expiry
        UserSummary user
) {}
