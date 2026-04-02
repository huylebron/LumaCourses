package com.luma.lumacourses.dto.auth;

import java.time.LocalDateTime;

public record MeResponse(
        Long userId,
        String username,
        String email,
        String fullName,
        String role,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
