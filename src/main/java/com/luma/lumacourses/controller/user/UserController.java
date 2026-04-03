package com.luma.lumacourses.controller.user;

import com.luma.lumacourses.dto.common.ApiResponse;
import com.luma.lumacourses.util.enums.Role;
import com.luma.lumacourses.dto.common.PagedData;
import com.luma.lumacourses.dto.common.PaginationMeta;
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


    /**
     * GET /api/users — ADMIN
     *  query params: role, active, page, size
     *
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List users (ADMIN)", parameters = {
            @Parameter(name = "role", description = "Filter by role (ADMIN, TEACHER, STUDENT)", example = "TEACHER"),
            @Parameter(name = "active", description = "Filter by active status", example = "true"),
            @Parameter(name = "page", description = "Page number", example = "0"),
            @Parameter(name = "size", description = "Page size", example = "10")
    })
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "TC-U1: Paginate user list "),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "TC-U2: Filtered by role/active"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "TC-U3:  Forbidden")
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
        return ResponseEntity.ok(ApiResponse.success(new PagedData<>(result.getContent(), meta), "Users list detail successfully "));
    }

    //  GET /api/users/{user_id}
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID (ADMIN)", description = "Returns full user details with userID .")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "TC-U4: User found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "TC-U5: User not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "TC-U6: Forbidden")
    })
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(userId), "User detail successfully "));
    }

    // POST /api/users/register

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
                .body(ApiResponse.success(created, "User register successfully"));
    }

    //  PUT /api/users/{user_id


    @PutMapping("/{userId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update user profile (OWNER or ADMIN)", description = "Update profile fields (username, email, fullName)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "TC-U10: Owner updates own profile"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "TC-U11: ADMIN updates any profile"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "TC-U12: Forbidden")
    })
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.updateProfile(userId, request, principal), "Profile updated"));
    }

    // PUT /api/users/{user_id}/password


    @PutMapping("/{userId}/password")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Change password (OWNER or ADMIN)", description = "Change the user's password.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "TC-U13: Owner changes own password successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "TC-U14: Wrong currentPassword"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "TC-U15: ADMIN bypass currentPassword ")
    })
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable Long userId,
            @Valid @RequestBody PasswordChangeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        userService.changePassword(userId, request, principal);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }

    // PUT /api/users/{user_id}/role
    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Change user role (ADMIN)", description = "Update a user's role")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "TC-U16: Role updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "TC-U17: Attempting to change another ADMIN's role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "TC-U18:  Forbidden")
    })
    public ResponseEntity<ApiResponse<UserResponse>> updateRole(
            @PathVariable Long userId,
            @Valid @RequestBody UserRoleUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.updateRole(userId, request, principal), "Role updated"));
    }

    // PUT /api/users/{user_id}/status


    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Enable/disable user (ADMIN)")
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

    // DELETE /api/users/{user_id}

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user (ADMIN, soft delete)", description = "Soft-delele account users ")
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
