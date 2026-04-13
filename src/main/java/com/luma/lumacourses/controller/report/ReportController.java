package com.luma.lumacourses.controller.report;

import com.luma.lumacourses.dto.common.ApiResponse;
import com.luma.lumacourses.dto.report.StudentProgressReportResponse;
import com.luma.lumacourses.dto.report.TeacherCoursesOverviewResponse;
import com.luma.lumacourses.dto.report.TopCourseReportResponse;
import com.luma.lumacourses.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Reports", description = "Admin reports and analytics")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/top_courses")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Top courses report (ADMIN)", description = "Returns the most popular courses sorted by enrollment count descending.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Top courses retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<List<TopCourseReportResponse>>> getTopCourses(
            @Parameter(description = "Number of top courses", example = "10")
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "limit must be >= 1")
            @Max(value = 100, message = "limit must be <= 100")
            int limit) {
        List<TopCourseReportResponse> response = reportService.getTopCourses(limit);
        return ResponseEntity.ok(ApiResponse.success(response, "Top courses report retrieved"));
    }

    @GetMapping("/student_progress/{student_id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Student progress report (ADMIN)", description = "Returns progress statistics for a specific student.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Student progress report retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Student not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "User is not a student")
    })
    public ResponseEntity<ApiResponse<StudentProgressReportResponse>> getStudentProgress(
            @PathVariable("student_id") Long studentId) {
        StudentProgressReportResponse response = reportService.getStudentProgress(studentId);
        return ResponseEntity.ok(ApiResponse.success(response, "Student progress report retrieved"));
    }

    @GetMapping("/teacher_courses_overview/{teacher_id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Teacher courses overview report (ADMIN)", description = "Returns overview statistics for courses managed by a specific teacher.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Teacher courses overview report retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Teacher not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "User is not a teacher")
    })
    public ResponseEntity<ApiResponse<TeacherCoursesOverviewResponse>> getTeacherCoursesOverview(
            @PathVariable("teacher_id") Long teacherId) {
        TeacherCoursesOverviewResponse response = reportService.getTeacherCoursesOverview(teacherId);
        return ResponseEntity.ok(ApiResponse.success(response, "Teacher courses overview report retrieved"));
    }
}
