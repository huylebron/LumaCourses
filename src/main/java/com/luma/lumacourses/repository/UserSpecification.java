package com.luma.lumacourses.repository;

import com.luma.lumacourses.common.enums.Role;
import com.luma.lumacourses.entity.User;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA Specifications for dynamic filtering of {@link User} entities.
 */
public final class UserSpecification {

    private UserSpecification() {
    }

    /**
     * Filter users by role. Returns no-op if role is null.
     */
    public static Specification<User> hasRole(Role role) {
        return (root, query, cb) -> role == null ? cb.conjunction() : cb.equal(root.get("role"), role);
    }

    /**
     * Filter users by active status. Returns no-op if active is null.
     */
    public static Specification<User> isActive(Boolean active) {
        return (root, query, cb) -> active == null ? cb.conjunction() : cb.equal(root.get("active"), active);
    }
}
