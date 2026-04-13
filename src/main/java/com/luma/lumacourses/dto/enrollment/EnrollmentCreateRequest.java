package com.luma.lumacourses.dto.enrollment;

import jakarta.validation.constraints.NotNull;

public record EnrollmentCreateRequest(
        @NotNull(message = "courseId is required") Long courseId) {
}
