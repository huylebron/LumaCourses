package com.luma.lumacourses.mapper;

import com.luma.lumacourses.dto.course.CourseDetailResponse;
import com.luma.lumacourses.dto.course.CourseResponse;
import com.luma.lumacourses.dto.course.CourseTeacherResponse;
import com.luma.lumacourses.dto.course.LessonSummaryResponse;
import com.luma.lumacourses.entity.Course;

import java.util.List;


public final class CourseMapper {

    private CourseMapper() {
    }

    public static CourseResponse toResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                toTeacherResponse(course),
                course.getPrice(),
                course.getDurationHours(),
                course.getStatus(),
                course.getCreatedAt(),
                course.getUpdatedAt());
    }

    public static CourseDetailResponse toDetailResponse(Course course, List<LessonSummaryResponse> lessons) {
        return new CourseDetailResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                toTeacherResponse(course),
                course.getPrice(),
                course.getDurationHours(),
                course.getStatus(),
                course.getCreatedAt(),
                course.getUpdatedAt(),
                lessons);
    }

    public static CourseTeacherResponse toTeacherResponse(Course course) {
        return new CourseTeacherResponse(
                course.getTeacher().getId(),
                course.getTeacher().getFullName(),
                course.getTeacher().getEmail());
    }
}
