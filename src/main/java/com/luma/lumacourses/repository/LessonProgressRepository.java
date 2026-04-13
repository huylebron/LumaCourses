package com.luma.lumacourses.repository;

import com.luma.lumacourses.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {

    boolean existsByLessonId(Long lessonId);

    Optional<LessonProgress> findByEnrollmentIdAndLessonId(Long enrollmentId, Long lessonId);

    List<LessonProgress> findByEnrollmentId(Long enrollmentId);

    long countByEnrollmentIdAndCompletedTrue(Long enrollmentId);
}
