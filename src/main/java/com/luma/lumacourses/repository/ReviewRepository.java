package com.luma.lumacourses.repository;

import com.luma.lumacourses.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByCourseId(Long courseId, Pageable pageable);

    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);

    long countByCourseTeacherId(Long teacherId);

    @Query("""
            select avg(r.rating)
            from Review r
            where r.course.teacher.id = :teacherId
            """)
    Double findAverageRatingByTeacherId(@Param("teacherId") Long teacherId);

    @Query("""
            select avg(r.rating)
            from Review r
            where r.course.id = :courseId
            """)
    Double findAverageRatingByCourseId(@Param("courseId") Long courseId);

    long countByCourseId(Long courseId);
}
