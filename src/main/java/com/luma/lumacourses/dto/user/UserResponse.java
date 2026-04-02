package com.luma.lumacourses.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
        Long id,
        String username,
        String email,
        String fullName,
        String role,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
