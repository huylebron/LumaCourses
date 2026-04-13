package com.luma.lumacourses.dto.report;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StudentProgressReportResponse(
        Long studentId,
        String studentName,
        String studentEmail,
        long totalEnrollments,
        long completedEnrollments,
        BigDecimal averageProgressPercentage,
        List<StudentCourseProgressResponse> courses) {
}

