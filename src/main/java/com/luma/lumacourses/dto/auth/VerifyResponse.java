package com.luma.lumacourses.dto.auth;

import java.time.Instant;

public record VerifyResponse(
        boolean valid,
        Long userId,
        String email,
        String role,
        Instant expiresAt
) {}
