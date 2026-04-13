package com.luma.lumacourses.service;

import com.luma.lumacourses.dto.report.StudentProgressReportResponse;
import com.luma.lumacourses.dto.report.TeacherCoursesOverviewResponse;
import com.luma.lumacourses.dto.report.TopCourseReportResponse;

import java.util.List;

public interface ReportService {

    List<TopCourseReportResponse> getTopCourses(int limit);

    StudentProgressReportResponse getStudentProgress(Long studentId);

    TeacherCoursesOverviewResponse getTeacherCoursesOverview(Long teacherId);
}

