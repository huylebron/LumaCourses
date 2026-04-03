package com.luma.lumacourses.controller.lesson;

import com.luma.lumacourses.dto.common.ApiResponse;
import com.luma.lumacourses.dto.course.LessonSummaryResponse;
import com.luma.lumacourses.dto.lesson.LessonContentPreviewResponse;
import com.luma.lumacourses.dto.lesson.LessonCreateRequest;
import com.luma.lumacourses.dto.lesson.LessonDetailResponse;
import com.luma.lumacourses.dto.lesson.LessonPublishRequest;
import com.luma.lumacourses.dto.lesson.LessonUpdateRequest;
import com.luma.lumacourses.security.principal.UserPrincipal;
import com.luma.lumacourses.service.LessonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Lessons", description = "Lesson management")
public class LessonController {

    private final LessonService lessonService;

    @GetMapping("/api/courses/{courseId}/lessons")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List lessons by course (AUTH)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lessons retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Course not found")
    })
    public ResponseEntity<ApiResponse<List<LessonSummaryResponse>>> listByCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<LessonSummaryResponse> response = lessonService.listByCourseId(courseId, principal);
        return ResponseEntity.ok(ApiResponse.success(response, "Lessons retrieved"));
    }

    @GetMapping("/api/lessons/{lessonId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get lesson detail (AUTH)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lesson detail"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Lesson not found")
    })
    public ResponseEntity<ApiResponse<LessonDetailResponse>> getById(
            @PathVariable Long lessonId,
            @AuthenticationPrincipal UserPrincipal principal) {
        LessonDetailResponse response = lessonService.getById(lessonId, principal);
        return ResponseEntity.ok(ApiResponse.success(response, "Lesson detail sussessfuilly"));
    }

    @PostMapping("/api/courses/{courseId}/lessons")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @Operation(summary = "Create lesson (TEACHER_OR_ADMIN)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Lesson created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Course not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Order index conflict")
    })
    public ResponseEntity<ApiResponse<LessonDetailResponse>> create(
            @PathVariable Long courseId,
            @Valid @RequestBody LessonCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        LessonDetailResponse response = lessonService.create(courseId, request, principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Lesson created successfully"));
    }

    @PutMapping("/api/lessons/{lessonId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @Operation(summary = "Update lesson (TEACHER_OR_ADMIN)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lesson updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Lesson not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Order index conflict")
    })
    public ResponseEntity<ApiResponse<LessonDetailResponse>> update(
            @PathVariable Long lessonId,
            @Valid @RequestBody LessonUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        LessonDetailResponse response = lessonService.update(lessonId, request, principal);
        return ResponseEntity.ok(ApiResponse.success(response, "Lesson updated"));
    }

    @PutMapping("/api/lessons/{lessonId}/publish")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @Operation(summary = "Update lesson publish status (TEACHER_OR_ADMIN)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Publish status updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Lesson not found")
    })
    public ResponseEntity<ApiResponse<LessonDetailResponse>> updatePublishStatus(
            @PathVariable Long lessonId,
            @Valid @RequestBody LessonPublishRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        LessonDetailResponse response = lessonService.updatePublishStatus(lessonId, request, principal);
        return ResponseEntity.ok(ApiResponse.success(response, "Lesson publish status updated"));
    }

    @DeleteMapping("/api/lessons/{lessonId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    @Operation(summary = "Delete lesson (TEACHER_OR_ADMIN)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lesson deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Lesson not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Lesson has progress")
    })
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long lessonId,
            @AuthenticationPrincipal UserPrincipal principal) {
        lessonService.delete(lessonId, principal);
        return ResponseEntity.ok(ApiResponse.success(null, "Lesson deleted successfully"));
    }

    @GetMapping("/api/lessons/{lessonId}/content_preview")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get lesson content preview (AUTH)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Content preview retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Lesson not found")
    })
    public ResponseEntity<ApiResponse<LessonContentPreviewResponse>> contentPreview(
            @PathVariable Long lessonId,
            @AuthenticationPrincipal UserPrincipal principal) {
        LessonContentPreviewResponse response = lessonService.getContentPreview(lessonId, principal);
        return ResponseEntity.ok(ApiResponse.success(response, "Lesson content preview retrieved"));
    }
}
