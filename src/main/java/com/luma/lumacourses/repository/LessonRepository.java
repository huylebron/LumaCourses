package com.luma.lumacourses.repository;

import com.luma.lumacourses.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByCourseIdOrderByOrderIndexAsc(Long courseId);

    List<Lesson> findByCourseIdAndPublishedTrueOrderByOrderIndexAsc(Long courseId);

    boolean existsByCourseIdAndOrderIndex(Long courseId, Integer orderIndex);

    boolean existsByCourseIdAndOrderIndexAndIdNot(Long courseId, Integer orderIndex, Long lessonId);
}
