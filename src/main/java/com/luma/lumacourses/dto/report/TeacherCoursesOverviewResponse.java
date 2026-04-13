package com.luma.lumacourses.dto.report;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TeacherCoursesOverviewResponse(
        Long teacherId,
        String teacherName,
        String teacherEmail,
        long totalCourses,
        long publishedCourses,
        long draftCourses,
        long archivedCourses,
        long totalEnrollments,
        long totalReviews,
        BigDecimal averageRating,
        List<TeacherCourseOverviewItemResponse> courses) {
}

