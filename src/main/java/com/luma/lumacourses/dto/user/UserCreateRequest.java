package com.luma.lumacourses.dto.user;

import com.luma.lumacourses.util.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**

 * POST /api/users/register
 */
public record UserCreateRequest(

        @NotBlank(message = "Username is required") @Size(min = 3, max = 50, message = "Username  3-50 characters") String username,

        @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email,

        @NotBlank(message = "Password is required") @Size(min = 8, message = "Password  8 characters") String password,

        @NotBlank(message = "Full name is required") @Size(max = 100, message = "Full name  100 characters") String fullName,

        @NotNull(message = "Role is required") Role role) {
}
