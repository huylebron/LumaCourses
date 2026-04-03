package com.luma.lumacourses.dto.lesson;

import jakarta.validation.constraints.NotNull;

public record LessonPublishRequest(

        @NotNull(message = "Published is required")
        Boolean published) {
}
