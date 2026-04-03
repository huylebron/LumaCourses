package com.luma.lumacourses.service;

import com.luma.lumacourses.util.enums.Role;
import com.luma.lumacourses.dto.user.*;
import com.luma.lumacourses.security.principal.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {


    UserResponse registerUser(UserRegisterRequest request);


    Page<UserResponse> listUsers(Role role, Boolean active, Pageable pageable);

    UserResponse getUserById(Long id);


    UserResponse createUser(UserCreateRequest request);

    UserResponse updateProfile(Long id, UserUpdateRequest request, UserPrincipal principal);

    void changePassword(Long id, PasswordChangeRequest request, UserPrincipal principal);


    UserResponse updateRole(Long id, UserRoleUpdateRequest request, UserPrincipal principal);


    UserResponse updateStatus(Long id, UserStatusUpdateRequest request);

    void deleteUser(Long id);
}
