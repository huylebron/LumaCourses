package com.luma.lumacourses.dto.course;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CourseCreateRequest(

        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        String description,

        @NotNull(message = "Teacher id is required")
        Long teacherId,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Price must be greater than or equal to 0")
        BigDecimal price,

        @Min(value = 0, message = "Duration hours must be greater than or equal to 0")
        Integer durationHours) {
}
