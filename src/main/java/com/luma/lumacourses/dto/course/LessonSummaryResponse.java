package com.luma.lumacourses.dto.course;

public record LessonSummaryResponse(
        Long id,
        String title,
        Integer orderIndex,
        boolean published) {
}
