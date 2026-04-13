package com.luma.lumacourses.dto.enrollment;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LessonProgressResponse(
        Long lessonId,
        String title,
        Integer orderIndex,
        boolean published,
        boolean completed,
        LocalDateTime completedAt,
        LocalDateTime lastAccessedAt) {
}
