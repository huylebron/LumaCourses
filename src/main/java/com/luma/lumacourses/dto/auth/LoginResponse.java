package com.luma.lumacourses.dto.auth;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,      //Bearer
        long expiresIn,
        UserSummary user
) {}
