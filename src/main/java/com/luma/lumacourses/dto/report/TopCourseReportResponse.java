package com.luma.lumacourses.dto.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.luma.lumacourses.util.enums.CourseStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TopCourseReportResponse(
        Long courseId,
        String courseTitle,
        Long teacherId,
        String teacherName,
        CourseStatus courseStatus,
        long enrollmentCount) {
}

