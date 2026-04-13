package com.luma.lumacourses.dto.enrollment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.luma.lumacourses.util.enums.EnrollmentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EnrollmentSummaryResponse(
        Long id,
        Long courseId,
        String courseTitle,
        Long teacherId,
        String teacherName,
        EnrollmentStatus status,
        BigDecimal progressPercentage,
        LocalDateTime enrollmentDate,
        LocalDateTime completionDate) {
}
