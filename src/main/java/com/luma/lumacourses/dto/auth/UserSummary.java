package com.luma.lumacourses.dto.auth;

public record UserSummary(
        Long userId,
        String username,
        String email,
        String fullName,
        String role
) {}
