package com.luma.lumacourses.service;

import com.luma.lumacourses.common.enums.Role;
import com.luma.lumacourses.dto.user.*;
import com.luma.lumacourses.security.principal.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    /**
     * Public registration.
     * Allowed roles: STUDENT (active=true), TEACHER (active=false).
     */
    UserResponse registerUser(UserRegisterRequest request);

    /**
     * List all users with optional role/active filters. ADMIN only.
     */
    Page<UserResponse> listUsers(Role role, Boolean active, Pageable pageable);

    /**
     * Get a single user by ID. ADMIN only.
     */
    UserResponse getUserById(Long id);

    /**
     * Create a new user. ADMIN only.
     * Business rules: any role can be assigned; TEACHER created as active=false
     * pending approval.
     */
    UserResponse createUser(UserCreateRequest request);

    /**
     * Update user profile fields. OWNER or ADMIN.
     */
    UserResponse updateProfile(Long id, UserUpdateRequest request, UserPrincipal principal);

    /**
     * Change password. OWNER or ADMIN.
     * OWNER must provide correct currentPassword. ADMIN can skip verification.
     */
    void changePassword(Long id, PasswordChangeRequest request, UserPrincipal principal);

    /**
     * Change user role. ADMIN only.
     * Cannot change the role of another ADMIN.
     */
    UserResponse updateRole(Long id, UserRoleUpdateRequest request, UserPrincipal principal);

    /**
     * Enable or disable a user account. ADMIN only.
     */
    UserResponse updateStatus(Long id, UserStatusUpdateRequest request);

    /**
     * Soft-delete a user (set is_active = false). ADMIN only.
     */
    void deleteUser(Long id);
}
