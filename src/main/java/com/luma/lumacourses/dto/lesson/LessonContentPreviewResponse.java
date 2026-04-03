package com.luma.lumacourses.dto.lesson;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LessonContentPreviewResponse(
        Long lessonId,
        String title,
        String contentUrl,
        String excerpt,
        boolean hasMore,
        LocalDateTime updatedAt) {
}
