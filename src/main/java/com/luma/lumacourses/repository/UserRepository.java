package com.luma.lumacourses.repository;

import com.luma.lumacourses.util.enums.Role;
import com.luma.lumacourses.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("""
            select u
            from User u
            where (:role is null or u.role = :role)
              and (:active is null or u.active = :active)
            """)
    Page<User> findAllByFilters(@Param("role") Role role, @Param("active") Boolean active, Pageable pageable);
}
