package com.luma.lumacourses.repository;

import com.luma.lumacourses.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByCourseId(Long courseId, Pageable pageable);

    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);
}

