package com.luma.lumacourses.controller.course;

import com.luma.lumacourses.dto.common.ApiResponse;
import com.luma.lumacourses.dto.common.PagedData;
import com.luma.lumacourses.dto.common.PaginationMeta;
import com.luma.lumacourses.dto.course.CourseCreateRequest;
import com.luma.lumacourses.dto.course.CourseDetailResponse;
import com.luma.lumacourses.dto.course.CourseResponse;
import com.luma.lumacourses.dto.course.CourseStatusUpdateRequest;
import com.luma.lumacourses.dto.course.CourseUpdateRequest;
import com.luma.lumacourses.security.principal.UserPrincipal;
import com.luma.lumacourses.service.CourseService;
import com.luma.lumacourses.util.enums.CourseStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Courses", description = "Course  management ")
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List courses (AUTH)", description = "Returns paginated courses with optional search/filter and role-based visibility.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Courses successfully"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated")
    })
    public ResponseEntity<ApiResponse<PagedData<CourseResponse>>> listCourses(
            @Parameter(description = "Search in title/description", example = "spring")
            @RequestParam(required = false) String search,
            @Parameter(name = "teacher_id", description = "Filter by teacher id", example = "2")
            @RequestParam(name = "teacher_id", required = false) Long teacherId,
            @Parameter(description = "Filter by course status", example = "PUBLISHED")
            @RequestParam(required = false) CourseStatus status,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal principal) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CourseResponse> result = courseService.listCourses(search, teacherId, status, pageable, principal);

        PaginationMeta meta = new PaginationMeta(
                result.getNumber(),
                result.getSize(),
                result.getTotalPages(),
                result.getTotalElements());

        return ResponseEntity.ok(ApiResponse.success(new PagedData<>(result.getContent(), meta), "Courses retrieved"));
    }

    @GetMapping("/{courseId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get course detail (AUTH)", description = "Returns course detail ")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Course retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Course not found")
    })
    public ResponseEntity<ApiResponse<CourseDetailResponse>> getCourseById(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal principal) {
        CourseDetailResponse response = courseService.getCourseById(courseId, principal);
        return ResponseEntity.ok(ApiResponse.success(response, "Course retrieved"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create course (ADMIN)", description = "Creates  new course witth status DRAFT.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Course created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Teacher not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Teacher invalid or inactive")
    })
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(
            @Valid @RequestBody CourseCreateRequest request) {
        CourseResponse response = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Course created successfully"));
    }

    @PutMapping("/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update course (ADMIN)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Course updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Course/teacher not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Teacher invalid or inactive")
    })
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseUpdateRequest request) {
        CourseResponse response = courseService.updateCourse(courseId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Course updated"));
    }

    @PutMapping("/{courseId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update course status (ADMIN)", description = "Updates status  course (DRAFT/PUBLISHED/ARCHIVED).")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Course not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourseStatus(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseStatusUpdateRequest request) {
        CourseResponse response = courseService.updateStatus(courseId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Course status updated"));
    }

    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete course (ADMIN)", description = "Soft-deletes ")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Course archived"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Course not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable Long courseId) {
        courseService.deleteCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success(null, "Course deleted successfully"));
    }
}
