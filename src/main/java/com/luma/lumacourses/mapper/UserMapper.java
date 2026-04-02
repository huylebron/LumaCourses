package com.luma.lumacourses.mapper;

import com.luma.lumacourses.dto.user.UserResponse;
import com.luma.lumacourses.entity.User;

/**
 * Static mapper — converts {@link User} entity to {@link UserResponse} DTO.
 */
public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
