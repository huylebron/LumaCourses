package com.luma.lumacourses.controller.auth;

import com.luma.lumacourses.dto.auth.*;
import com.luma.lumacourses.dto.common.ApiResponse;
import com.luma.lumacourses.security.principal.UserPrincipal;
import com.luma.lumacourses.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login · Verify · Profile · Logout · Refresh")
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/login
     */
    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate and receive JWT access + refresh tokens")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(authService.login(request), "Login successful"));
    }

    /**
     * POST /api/auth/verify
     *
     */
    @PostMapping("/verify")
    @Operation(summary = "Verify token", description = "Check token validation and return  claims")
    public ResponseEntity<ApiResponse<VerifyResponse>> verify(
            @Valid @RequestBody VerifyRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(authService.verify(request), "Token verified"));
    }

    /**
     * GET /api/auth/me
     * R
     */
    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get my profile", description = "Returns full profile of the authenticated user")
    public ResponseEntity<ApiResponse<MeResponse>> me(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                ApiResponse.success(authService.me(principal), "Profile retrieved"));
    }

    /**
     * POST /api/auth/logout
     * blacklist token and revoke token
     */
    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Logout", description = "Invalidate access token blacklist + revoke refresh token")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody LogoutRequest request) {
        String rawToken = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        authService.logout(rawToken, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }

    /**
     * POST /api/auth/refresh
     *
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh tokens",
            description = "Exchange refresh token for new access + refresh token ")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(authService.refresh(request), "Tokens refreshed successfully"));
    }
}
