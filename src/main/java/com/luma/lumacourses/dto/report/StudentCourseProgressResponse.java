package com.luma.lumacourses.dto.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.luma.lumacourses.util.enums.EnrollmentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StudentCourseProgressResponse(
        Long enrollmentId,
        Long courseId,
        String courseTitle,
        EnrollmentStatus status,
        BigDecimal progressPercentage,
        LocalDateTime enrollmentDate,
        LocalDateTime completionDate) {
}

