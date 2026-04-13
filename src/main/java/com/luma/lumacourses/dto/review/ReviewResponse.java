package com.luma.lumacourses.dto.review;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReviewResponse(
        Long id,
        Long courseId,
        Long studentId,
        String studentName,
        int rating,
        String comment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}

