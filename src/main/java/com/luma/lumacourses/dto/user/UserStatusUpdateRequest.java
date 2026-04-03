package com.luma.lumacourses.dto.user;

import jakarta.validation.constraints.NotNull;

/**

 * PUT /api/users/{user_id}/status
 */
public record UserStatusUpdateRequest(

        @NotNull(message = "Active status is required") Boolean active) {
}
