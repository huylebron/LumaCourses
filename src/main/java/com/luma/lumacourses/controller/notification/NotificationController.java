package com.luma.lumacourses.controller.notification;

import com.luma.lumacourses.dto.common.ApiResponse;
import com.luma.lumacourses.dto.common.PagedData;
import com.luma.lumacourses.dto.common.PaginationMeta;
import com.luma.lumacourses.dto.notification.NotificationCreateRequest;
import com.luma.lumacourses.dto.notification.NotificationResponse;
import com.luma.lumacourses.security.principal.UserPrincipal;
import com.luma.lumacourses.service.NotificationService;
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
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List my notifications (AUTH)", description = "Returns a paginated list of notifications for the authenticated user.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notifications retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated")
    })
    public ResponseEntity<ApiResponse<PagedData<NotificationResponse>>> listMyNotifications(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal principal) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<NotificationResponse> result = notificationService.listMyNotifications(principal, pageable);

        PaginationMeta meta = new PaginationMeta(
                result.getNumber(),
                result.getSize(),
                result.getTotalPages(),
                result.getTotalElements());

        return ResponseEntity.ok(ApiResponse.success(new PagedData<>(result.getContent(), meta), "Notifications retrieved"));
    }

    @PutMapping("/{notificationId}/read")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','STUDENT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Mark notification as read (AUTH)", description = "Marks the authenticated user's notification as read. Idempotent operation.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notification marked as read"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Notification not found")
    })
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserPrincipal principal) {

        NotificationResponse response = notificationService.markAsRead(notificationId, principal);
        return ResponseEntity.ok(ApiResponse.success(response, "Notification marked as read"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create notification (ADMIN)", description = "Creates a notification for a specific user.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Notification created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(
            @Valid @RequestBody NotificationCreateRequest request) {

        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Notification created"));
    }

    @DeleteMapping("/{notificationId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete notification (ADMIN)", description = "Deletes a notification by id.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notification deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Notification not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification deleted"));
    }
}
