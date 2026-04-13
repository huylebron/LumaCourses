package com.luma.lumacourses.controller.review;

import com.luma.lumacourses.dto.common.ApiResponse;
import com.luma.lumacourses.dto.common.PagedData;
import com.luma.lumacourses.dto.common.PaginationMeta;
import com.luma.lumacourses.dto.review.ReviewCreateRequest;
import com.luma.lumacourses.dto.review.ReviewResponse;
import com.luma.lumacourses.dto.review.ReviewUpdateRequest;
import com.luma.lumacourses.security.principal.UserPrincipal;
import com.luma.lumacourses.service.ReviewService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Course review management")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/api/courses/{courseId}/reviews")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List course reviews (AUTH)", description = "Returns paginated reviews for a course if the authenticated user can view that course.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reviews retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Course not found")
    })
    public ResponseEntity<ApiResponse<PagedData<ReviewResponse>>> listCourseReviews(
            @PathVariable Long courseId,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal principal) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReviewResponse> result = reviewService.listCourseReviews(courseId, pageable, principal);

        PaginationMeta meta = new PaginationMeta(
                result.getNumber(),
                result.getSize(),
                result.getTotalPages(),
                result.getTotalElements());

        return ResponseEntity.ok(ApiResponse.success(new PagedData<>(result.getContent(), meta), "Reviews retrieved"));
    }

    @PostMapping("/api/courses/{courseId}/reviews")
    @PreAuthorize("hasRole('STUDENT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create review (STUDENT)", description = "Creates a review for a completed course by the authenticated student.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Review created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Course not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Course not completed or review already exists")
    })
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @PathVariable Long courseId,
            @Valid @RequestBody ReviewCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        ReviewResponse response = reviewService.createReview(courseId, request, principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Review created"));
    }

    @PutMapping("/api/reviews/{reviewId}")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update review (OWNER_OR_ADMIN)", description = "Updates review rating/comment by owner or admin.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Review updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        ReviewResponse response = reviewService.updateReview(reviewId, request, principal);
        return ResponseEntity.ok(ApiResponse.success(response, "Review updated"));
    }

    @DeleteMapping("/api/reviews/{reviewId}")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete review (OWNER_OR_ADMIN)", description = "Deletes a review by owner or admin.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Review deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserPrincipal principal) {
        reviewService.deleteReview(reviewId, principal);
        return ResponseEntity.ok(ApiResponse.success(null, "Review deleted"));
    }
}

