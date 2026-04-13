package com.luma.lumacourses.repository;

import com.luma.lumacourses.entity.Enrollment;
import com.luma.lumacourses.util.enums.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Page<Enrollment> findByStudentId(Long studentId, Pageable pageable);

    Optional<Enrollment> findByIdAndStudentId(Long id, Long studentId);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    boolean existsByStudentIdAndCourseIdAndStatus(Long studentId, Long courseId, EnrollmentStatus status);
}
