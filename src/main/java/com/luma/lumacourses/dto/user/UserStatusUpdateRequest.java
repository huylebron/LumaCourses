package com.luma.lumacourses.dto.user;

import jakarta.validation.constraints.NotNull;

/**
 * Used by ADMIN to enable/disable a user account.
 * PUT /api/users/{user_id}/status
 */
public record UserStatusUpdateRequest(

        @NotNull(message = "Active status is required") Boolean active) {
}
