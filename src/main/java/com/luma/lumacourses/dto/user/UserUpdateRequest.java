package com.luma.lumacourses.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Used by OWNER or ADMIN to update a user's profile.
 * PUT /api/users/{user_id}
 * All fields are optional (nullable) — only non-null values are applied.
 */
public record UserUpdateRequest(

        @Size(min = 3, max = 50, message = "Username must be 3-50 characters") String username,

        @Email(message = "Email must be valid") String email,

        @Size(max = 100, message = "Full name must not exceed 100 characters") String fullName) {
}
