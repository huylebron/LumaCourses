package com.luma.lumacourses.mapper;

import com.luma.lumacourses.dto.report.StudentCourseProgressResponse;
import com.luma.lumacourses.dto.report.TeacherCourseOverviewItemResponse;
import com.luma.lumacourses.dto.report.TopCourseReportResponse;
import com.luma.lumacourses.entity.Course;
import com.luma.lumacourses.entity.Enrollment;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ReportMapper {

    private ReportMapper() {
    }

    public static TopCourseReportResponse toTopCourseReportResponse(Course course, long enrollmentCount) {
        return new TopCourseReportResponse(
                course.getId(),
                course.getTitle(),
                course.getTeacher().getId(),
                course.getTeacher().getFullName(),
                course.getStatus(),
                enrollmentCount);
    }

    public static StudentCourseProgressResponse toStudentCourseProgressResponse(Enrollment enrollment) {
        return new StudentCourseProgressResponse(
                enrollment.getId(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getTitle(),
                enrollment.getStatus(),
                enrollment.getProgressPercentage(),
                enrollment.getEnrollmentDate(),
                enrollment.getCompletionDate());
    }

    public static TeacherCourseOverviewItemResponse toTeacherCourseOverviewItemResponse(
            Course course,
            long enrollmentCount,
            long reviewCount,
            Double averageRating) {
        return new TeacherCourseOverviewItemResponse(
                course.getId(),
                course.getTitle(),
                course.getStatus(),
                enrollmentCount,
                reviewCount,
                toScaledDecimal(averageRating),
                course.getCreatedAt());
    }

    public static BigDecimal toScaledDecimal(Double value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}
