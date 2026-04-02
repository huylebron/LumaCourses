package com.luma.lumacourses.dto.user;

import com.luma.lumacourses.common.enums.Role;
import jakarta.validation.constraints.NotNull;

/**
 * Used by ADMIN to update a user's role.
 * PUT /api/users/{user_id}/role
 */
public record UserRoleUpdateRequest(

        @NotNull(message = "Role is required") Role role) {
}
