package com.luma.lumacourses.dto.user;

import com.luma.lumacourses.common.enums.Role;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Public registration request.
 * Only STUDENT and TEACHER roles are allowed.
 */
public record UserRegisterRequest(

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,

        @NotBlank(message = "Full name is required")
        @Size(max = 100, message = "Full name must not exceed 100 characters")
        String fullName,

        @NotNull(message = "Role is required")
        Role role) {

    @AssertTrue(message = "Role must be STUDENT or TEACHER for public registration")
    public boolean isPublicRole() {
        return role == Role.STUDENT || role == Role.TEACHER;
    }
}
