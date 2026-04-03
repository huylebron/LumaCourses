package com.luma.lumacourses.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 *
 * PUT /api/users/{user_id}/password
 */
public record PasswordChangeRequest(

        String currentPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "New password > 8 characters")
        String newPassword) {
}
