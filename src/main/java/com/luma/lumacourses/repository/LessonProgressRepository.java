package com.luma.lumacourses.repository;

import com.luma.lumacourses.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {

    boolean existsByLessonId(Long lessonId);
}
