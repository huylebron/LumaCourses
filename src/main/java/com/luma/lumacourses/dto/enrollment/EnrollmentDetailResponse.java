package com.luma.lumacourses.dto.enrollment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.luma.lumacourses.util.enums.EnrollmentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EnrollmentDetailResponse(
        Long id,
        Long courseId,
        String courseTitle,
        Long teacherId,
        String teacherName,
        EnrollmentStatus status,
        BigDecimal progressPercentage,
        int totalLessons,
        int completedLessons,
        LocalDateTime enrollmentDate,
        LocalDateTime completionDate,
        List<LessonProgressResponse> lessons) {
}
