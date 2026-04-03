package com.luma.lumacourses.dto.user;

import com.luma.lumacourses.util.enums.Role;
import jakarta.validation.constraints.NotNull;

/**

 * PUT /api/users/{user_id}/role
 */
public record UserRoleUpdateRequest(

        @NotNull(message = "Role is required") Role role) {
}
