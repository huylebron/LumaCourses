package com.luma.lumacourses.controller.enrollment;

import com.luma.lumacourses.dto.common.ApiResponse;
import com.luma.lumacourses.dto.common.PagedData;
import com.luma.lumacourses.dto.common.PaginationMeta;
import com.luma.lumacourses.dto.enrollment.EnrollmentCreateRequest;
import com.luma.lumacourses.dto.enrollment.EnrollmentDetailResponse;
import com.luma.lumacourses.dto.enrollment.EnrollmentSummaryResponse;
import com.luma.lumacourses.security.principal.UserPrincipal;
import com.luma.lumacourses.service.EnrollmentService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
@Tag(name = "Enrollments", description = "Enrollment and lesson progress management (STUDENT only)")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    // ─────────────────────────────────────────────────────────────
    // GET /api/enrollments
    // ─────────────────────────────────────────────────────────────

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List my enrollments (STUDENT)", description = "Returns a paginated list of all enrollments belonging to the authenticated student. "
            +
            "Sorted by enrollmentDate DESC.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Enrollments retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden – STUDENT role required")
    })
    public ResponseEntity<ApiResponse<PagedData<EnrollmentSummaryResponse>>> listMyEnrollments(
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal principal) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("enrollmentDate").descending());
        Page<EnrollmentSummaryResponse> result = enrollmentService.listMyEnrollments(principal, pageable);

        PaginationMeta meta = new PaginationMeta(
                result.getNumber(),
                result.getSize(),
                result.getTotalPages(),
                result.getTotalElements());

        return ResponseEntity
                .ok(ApiResponse.success(new PagedData<>(result.getContent(), meta), "Enrollments retrieved"));
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/enrollments
    // ─────────────────────────────────────────────────────────────

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Enroll in a course (STUDENT)", description = "Creates a new enrollment for the authenticated student. "
            +
            "The course must be PUBLISHED. Duplicate enrollments return 409.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Enrollment created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error – courseId missing"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden – STUDENT role required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Course not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Course is not PUBLISHED, or already enrolled")
    })
    public ResponseEntity<ApiResponse<EnrollmentDetailResponse>> createEnrollment(
            @Valid @RequestBody EnrollmentCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        EnrollmentDetailResponse response = enrollmentService.createEnrollment(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Enrolled successfully"));
    }

    // ─────────────────────────────────────────────────────────────
    // GET /api/enrollments/{enrollmentId}
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/{enrollmentId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get enrollment detail (STUDENT)", description = "Returns full detail of the student's own enrollment including published lessons and progress. "
            +
            "Returns 404 if the enrollment does not belong to the authenticated student.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Enrollment detail retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden – STUDENT role required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Enrollment not found or does not belong to the authenticated student")
    })
    public ResponseEntity<ApiResponse<EnrollmentDetailResponse>> getMyEnrollment(
            @PathVariable Long enrollmentId,
            @AuthenticationPrincipal UserPrincipal principal) {

        EnrollmentDetailResponse response = enrollmentService.getMyEnrollment(enrollmentId, principal);
        return ResponseEntity.ok(ApiResponse.success(response, "Enrollment retrieved"));
    }

    // ─────────────────────────────────────────────────────────────
    // PUT /api/enrollments/{enrollmentId}/complete_lesson/{lessonId}
    // ─────────────────────────────────────────────────────────────

    @PutMapping("/{enrollmentId}/complete_lesson/{lessonId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Mark a lesson as completed (STUDENT)", description = "Idempotent – calling this endpoint multiple times for the same lesson is safe and always returns 200. "
            +
            "The lesson must be published and belong to the enrolled course. " +
            "Progress percentage and enrollment status are recalculated automatically. " +
            "Returns 409 if the lesson is not published or does not belong to the enrolled course.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lesson marked as completed, progress updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden – STUDENT role required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Enrollment or lesson not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Lesson is not published or does not belong to the enrolled course")
    })
    public ResponseEntity<ApiResponse<EnrollmentDetailResponse>> completeLesson(
            @PathVariable Long enrollmentId,
            @PathVariable Long lessonId,
            @AuthenticationPrincipal UserPrincipal principal) {

        EnrollmentDetailResponse response = enrollmentService.completeLesson(enrollmentId, lessonId, principal);
        return ResponseEntity.ok(ApiResponse.success(response, "Lesson completed"));
    }
}
