package com.luma.lumacourses.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**

 * PUT /api/users/{user_id}
 */
public record UserUpdateRequest(

        @Size(min = 3, max = 50, message = "Username must be 3-50 characters") String username,

        @Email(message = "Email must be valid") String email,

        @Size(max = 100, message = "Full name must not exceed 100 characters") String fullName) {
}
