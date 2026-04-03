package com.luma.lumacourses.dto.course;

import com.luma.lumacourses.util.enums.CourseStatus;
import jakarta.validation.constraints.NotNull;

public record CourseStatusUpdateRequest(

        @NotNull(message = "Status is required")
        CourseStatus status) {
}
