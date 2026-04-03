package com.luma.lumacourses.dto.lesson;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LessonDetailResponse(
        Long id,
        Long courseId,
        String title,
        String contentUrl,
        String textContent,
        Integer orderIndex,
        boolean published,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
