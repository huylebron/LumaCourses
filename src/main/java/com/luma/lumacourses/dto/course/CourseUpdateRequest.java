package com.luma.lumacourses.dto.course;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CourseUpdateRequest(

        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        String description,

        Long teacherId,

        @DecimalMin(value = "0.0", inclusive = true, message = "Price must be greater than or equal to 0")
        BigDecimal price,

        @Min(value = 0, message = "Duration hours must be greater than or equal to 0")
        Integer durationHours) {
}
