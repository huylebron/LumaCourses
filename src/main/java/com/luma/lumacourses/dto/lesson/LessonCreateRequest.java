package com.luma.lumacourses.dto.lesson;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LessonCreateRequest(

        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        @Size(max = 500, message = "Content URL must not exceed 500 characters")
        String contentUrl,

        String textContent,

        @NotNull(message = "Order index is required")
        @Min(value = 1, message = "Order index must be greater than or equal to 1")
        Integer orderIndex) {
}
