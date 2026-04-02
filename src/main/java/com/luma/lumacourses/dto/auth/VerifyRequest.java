package com.luma.lumacourses.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record VerifyRequest(
        @NotBlank(message = "Token is required")
        String token
) {}
