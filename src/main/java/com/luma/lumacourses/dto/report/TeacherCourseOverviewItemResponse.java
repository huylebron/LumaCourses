package com.luma.lumacourses.dto.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.luma.lumacourses.util.enums.CourseStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TeacherCourseOverviewItemResponse(
        Long courseId,
        String courseTitle,
        CourseStatus status,
        long enrollmentCount,
        long reviewCount,
        BigDecimal averageRating,
        LocalDateTime createdAt) {
}

