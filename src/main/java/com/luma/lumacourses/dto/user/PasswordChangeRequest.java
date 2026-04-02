package com.luma.lumacourses.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Used by OWNER or ADMIN to change a user's password.
 * PUT /api/users/{user_id}/password
 */
public record PasswordChangeRequest(

        String currentPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "New password must be at least 8 characters")
        String newPassword) {
}
