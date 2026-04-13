package com.luma.lumacourses.repository;

import com.luma.lumacourses.entity.Course;
import com.luma.lumacourses.util.enums.CourseStatus;
import com.luma.lumacourses.util.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query("""
            SELECT c
            FROM Course c
            WHERE
                (:search IS NULL
                 OR lower(c.title) LIKE lower(concat('%', :search, '%'))
                 OR lower(coalesce(c.description, '')) LIKE lower(concat('%', :search, '%')))
                AND (:teacherId IS NULL OR c.teacher.id = :teacherId)
                AND (:status IS NULL OR c.status = :status)
                AND (
                    :role = com.luma.lumacourses.util.enums.Role.ADMIN
                    OR (:role = com.luma.lumacourses.util.enums.Role.STUDENT
                        AND c.status = com.luma.lumacourses.util.enums.CourseStatus.PUBLISHED)
                    OR (:role = com.luma.lumacourses.util.enums.Role.TEACHER
                        AND (c.status = com.luma.lumacourses.util.enums.CourseStatus.PUBLISHED
                             OR c.teacher.id = :principalUserId))
                )
            """)
    Page<Course> findVisibleCourses(@Param("search") String search,
                                    @Param("teacherId") Long teacherId,
                                    @Param("status") CourseStatus status,
                                    @Param("role") Role role,
                                    @Param("principalUserId") Long principalUserId,
                                    Pageable pageable);

    List<Course> findByTeacherIdOrderByCreatedAtDesc(Long teacherId);

    List<Course> findAllByOrderByCreatedAtDesc();
}
