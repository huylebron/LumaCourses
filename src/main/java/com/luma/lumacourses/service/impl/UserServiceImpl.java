package com.luma.lumacourses.service.impl;

import com.luma.lumacourses.util.enums.Role;
import com.luma.lumacourses.util.exception.ConflictException;
import com.luma.lumacourses.dto.user.*;
import com.luma.lumacourses.entity.User;
import com.luma.lumacourses.mapper.UserMapper;
import com.luma.lumacourses.repository.UserRepository;
import com.luma.lumacourses.security.principal.UserPrincipal;
import com.luma.lumacourses.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // List



    @Override
    public UserResponse registerUser(UserRegisterRequest request) {
        return createUserInternal(
                request.username(),
                request.email(),
                request.password(),
                request.fullName(),
                request.role(),
                true);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(Role role, Boolean active, Pageable pageable) {
        return userRepository.findAllByFilters(role, active, pageable).map(UserMapper::toResponse);
    }

    // Get by ID ─

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = findUserOrThrow(id);
        return UserMapper.toResponse(user);
    }

    // Create ─

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        return createUserInternal(
                request.username(),
                request.email(),
                request.password(),
                request.fullName(),
                request.role(),
                false);
    }




    @Override
    public UserResponse updateProfile(Long id, UserUpdateRequest request, UserPrincipal principal) {
        checkOwnerOrAdmin(id, principal);
        User user = findUserOrThrow(id);

        if (request.username() != null && !request.username().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.username())) {
                throw new ConflictException("Username already taken: " + request.username());
            }
            user.setUsername(request.username());
        }
        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new ConflictException("Email already in use: " + request.email());
            }
            user.setEmail(request.email());
        }
        if (request.fullName() != null) {
            user.setFullName(request.fullName());
        }

        return UserMapper.toResponse(userRepository.save(user));
    }

   //

    @Override
    public void changePassword(Long id, PasswordChangeRequest request, UserPrincipal principal) {
        checkOwnerOrAdmin(id, principal);
        User user = findUserOrThrow(id);

        // When an ADMIN changes another user's password, the currentPassword check can
        // be skipped
        boolean isOwner = principal.getUserId().equals(id);
        if (isOwner) {
            if (request.currentPassword() == null || request.currentPassword().isBlank()) {
                throw new AccessDeniedException("Current password is required");
            }
            if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
                throw new AccessDeniedException("Current password is incorrect");
            }
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        log.info("Password changed for userId={} by principalId={}", id, principal.getUserId());
    }

    // ─── Update Role (ADMIN) ──────────────────────────────────────────────────

    @Override
    public UserResponse updateRole(Long id, UserRoleUpdateRequest request, UserPrincipal principal) {
        User target = findUserOrThrow(id);

        // An admin cannot change the role of another admin
        if (target.getRole() == Role.ADMIN && !target.getId().equals(principal.getUserId())) {
            throw new AccessDeniedException("Cannot change the role of another ADMIN");
        }

        target.setRole(request.role());
        return UserMapper.toResponse(userRepository.save(target));
    }

    // ─── Update Status (ADMIN) ────────────────────────────────────────────────

    @Override
    public UserResponse updateStatus(Long id, UserStatusUpdateRequest request) {
        User user = findUserOrThrow(id);
        user.setActive(request.active());
        return UserMapper.toResponse(userRepository.save(user));
    }

    // ─── Delete (ADMIN, soft delete) ──────────────────────────────────────────

    @Override
    public void deleteUser(Long id) {
        User user = findUserOrThrow(id);
        user.setActive(false);
        userRepository.save(user);
        log.info("User soft-deleted: id={}", id);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    /**
     * Throws AccessDeniedException if the calling principal is neither the owner
     * nor ADMIN.
     */
    private void checkOwnerOrAdmin(Long targetId, UserPrincipal principal) {
        boolean isOwner = principal.getUserId().equals(targetId);
        boolean isAdmin = principal.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You are not allowed to modify this user");
        }
    }

    private UserResponse createUserInternal(String username,
            String email,
            String password,
            String fullName,
            Role role,
            boolean publicRegistration) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already in use: " + email);
        }
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("Username already taken: " + username);
        }

        if (publicRegistration && role != Role.STUDENT && role != Role.TEACHER) {
            throw new AccessDeniedException("Public registration only supports STUDENT or TEACHER");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setRole(role);
        user.setActive(role != Role.TEACHER);

        User saved = userRepository.save(user);
        if (publicRegistration) {
            log.info("Public registration created user: id={}, role={}", saved.getId(), saved.getRole());
        } else {
            log.info("User created by admin: id={}, role={}", saved.getId(), saved.getRole());
        }
        return UserMapper.toResponse(saved);
    }
}
