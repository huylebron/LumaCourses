package com.luma.lumacourses.controller;

import com.luma.lumacourses.common.enums.Role;
import com.luma.lumacourses.dto.ApiResponse;
import com.luma.lumacourses.dto.PagedData;
import com.luma.lumacourses.dto.PaginationMeta;
import com.luma.lumacourses.dto.user.*;
import com.luma.lumacourses.security.principal.UserPrincipal;
import com.luma.lumacourses.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management — ADMIN operations and self-service profile/password")
public class UserController {

    private final UserService userService;

    // ─── GET /api/users ───────────────────────────────────────────────────────

    /**
     * GET /api/users — ADMIN only
     * Supports optional query params: role, active, page, size
     *
     * TC-U1: ADMIN lists all users → 200 page result
     * TC-U2: ADMIN filters by role=TEACHER → only teachers returned
     * TC-U3: Non-admin token → 403 Forbidden
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List users (ADMIN)", description = "Returns a paginated list of all users. Supports optional filtering by `role` (ADMIN/TEACHER/STUDENT) and `active` status.", parameters = {
            @Parameter(name = "role", description = "Filter by role (ADMIN, TEACHER, STUDENT)", example = "TEACHER"),
            @Parameter(name = "active", description = "Filter by active status (true/false)", example = "true"),
            @Parameter(name = "page", description = "Page number (0-based)", example = "0"),
            @Parameter(name = "size", description = "Page size", example = "10")
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "TC-U1: Paginated user list returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "TC-U2: Filtered by role/active"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "TC-U3: Non-admin access → Forbidden")
    })
    public ResponseEntity<ApiResponse<PagedData<UserResponse>>> listUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<UserResponse> result = userService.listUsers(role, active, pageable);

        PaginationMeta meta = new PaginationMeta(
                result.getNumber(),
                result.getSize(),
                result.getTotalPages(),
                result.getTotalElements());
        return ResponseEntity.ok(ApiResponse.success(new PagedData<>(result.getContent(), meta), "Users retrieved"));
    }

    // ─── GET /api/users/{user_id} ─────────────────────────────────────────────

    /**
     * TC-U4: ADMIN gets existing user → 200 with user data
     * TC-U5: ADMIN gets non-existent user → 404 Not Found
     * TC-U6: Non-admin token → 403 Forbidden
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID (ADMIN)", description = "Returns full user details for the specified user ID.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "TC-U4: User found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "TC-U5: User not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "TC-U6: Non-admin → Forbidden")
    })
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(userId), "User retrieved"));
    }

    // ─── POST /api/users/register ─────────────────────────────────────────────

    /**
     * TC-U7: PUBLIC registers STUDENT → 201, active=true
     * TC-U8: PUBLIC registers TEACHER → 201, active=false (pending approval)
     * TC-U9: Duplicate email → 409 Conflict
     */
    @PostMapping("/register")
    @Operation(summary = "Public register", description = "Public registration for STUDENT and TEACHER. TEACHER accounts start as `active=false` pending admin approval.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
            @ExampleObject(name = "Register STUDENT", value = "{\"username\":\"john_doe\",\"email\":\"john@example.com\",\"password\":\"secret123\",\"fullName\":\"John Doe\",\"role\":\"STUDENT\"}"),
            @ExampleObject(name = "Register TEACHER", value = "{\"username\":\"jane_teach\",\"email\":\"jane@example.com\",\"password\":\"secret123\",\"fullName\":\"Jane Smith\",\"role\":\"TEACHER\"}")
    })))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "TC-U7: STUDENT registered → active=true"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "TC-U8: TEACHER registered → active=false"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "TC-U9: Duplicate email/username → Conflict")
    })
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody UserRegisterRequest request) {
        UserResponse created = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "User registered successfully"));
    }

    // ─── PUT /api/users/{user_id} ─────────────────────────────────────────────

    /**
     * TC-U10: Owner updates own fullName → 200 updated
     * TC-U11: ADMIN updates another user → 200 updated
     * TC-U12: User tries to update another user's profile → 403
     */
    @PutMapping("/{userId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update user profile (OWNER or ADMIN)", description = "Update profile fields (username, email, fullName). Only non-null fields are applied. Owner or ADMIN only.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "TC-U10: Owner updates own profile"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "TC-U11: ADMIN updates any profile"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "TC-U12: Non-owner/non-admin → Forbidden")
    })
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.updateProfile(userId, request, principal), "Profile updated"));
    }

    // ─── PUT /api/users/{user_id}/password ───────────────────────────────────

    /**
     * TC-U13: Owner provides correct currentPassword → 200 OK
     * TC-U14: Owner provides wrong currentPassword → 403
     * TC-U15: ADMIN changes another user's password (no currentPassword check) →
     * 200
     */
    @PutMapping("/{userId}/password")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Change password (OWNER or ADMIN)", description = "Change the user's password. When the owner calls this endpoint, `currentPassword` is verified. ADMIN can bypass the current password check.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "TC-U13: Owner changes own password successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "TC-U14: Wrong currentPassword"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "TC-U15: ADMIN bypasses currentPassword check")
    })
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable Long userId,
            @Valid @RequestBody PasswordChangeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        userService.changePassword(userId, request, principal);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }

    // ─── PUT /api/users/{user_id}/role ────────────────────────────────────────

    /**
     * TC-U16: ADMIN changes STUDENT → TEACHER → 200
     * TC-U17: ADMIN tries to change another ADMIN's role → 403
     * TC-U18: Non-admin token → 403
     */
    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Change user role (ADMIN)", description = "Update a user's role. An ADMIN is not permitted to change the role of another ADMIN user.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "TC-U16: Role updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "TC-U17: Attempting to change another ADMIN's role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "TC-U18: Non-admin token → Forbidden")
    })
    public ResponseEntity<ApiResponse<UserResponse>> updateRole(
            @PathVariable Long userId,
            @Valid @RequestBody UserRoleUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.updateRole(userId, request, principal), "Role updated"));
    }

    // ─── PUT /api/users/{user_id}/status ──────────────────────────────────────

    /**
     * TC-U19: ADMIN disables active user → 200, active=false
     * TC-U20: ADMIN enables inactive user → 200, active=true
     * TC-U21: Non-admin token → 403
     */
    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Enable/disable user (ADMIN)", description = "Toggle the `is_active` flag of a user account.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "TC-U19: User disabled → active=false"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "TC-U20: User enabled → active=true"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "TC-U21: Non-admin → Forbidden")
    })
    public ResponseEntity<ApiResponse<UserResponse>> updateStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UserStatusUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.updateStatus(userId, request), "User status updated"));
    }

    // ─── DELETE /api/users/{user_id} ──────────────────────────────────────────

    /**
     * TC-U22: ADMIN deletes existing user → 200, user active=false
     * TC-U23: ADMIN deletes non-existent user → 404
     * TC-U24: Non-admin token → 403
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user (ADMIN, soft delete)", description = "Soft-deletes a user by setting `is_active = false`. The user record is retained in the database.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "TC-U22: User soft-deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "TC-U23: User not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "TC-U24: Non-admin → Forbidden")
    })
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }
}
