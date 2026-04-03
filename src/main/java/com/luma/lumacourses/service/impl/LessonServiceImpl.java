package com.luma.lumacourses.service.impl;

import com.luma.lumacourses.dto.course.LessonSummaryResponse;
import com.luma.lumacourses.dto.lesson.LessonContentPreviewResponse;
import com.luma.lumacourses.dto.lesson.LessonCreateRequest;
import com.luma.lumacourses.dto.lesson.LessonDetailResponse;
import com.luma.lumacourses.dto.lesson.LessonPublishRequest;
import com.luma.lumacourses.dto.lesson.LessonUpdateRequest;
import com.luma.lumacourses.entity.Course;
import com.luma.lumacourses.entity.Lesson;
import com.luma.lumacourses.mapper.LessonMapper;
import com.luma.lumacourses.repository.CourseRepository;
import com.luma.lumacourses.repository.LessonProgressRepository;
import com.luma.lumacourses.repository.LessonRepository;
import com.luma.lumacourses.security.principal.UserPrincipal;
import com.luma.lumacourses.service.LessonService;
import com.luma.lumacourses.util.enums.CourseStatus;
import com.luma.lumacourses.util.enums.Role;
import com.luma.lumacourses.util.exception.ConflictException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LessonServiceImpl implements LessonService {

    private static final int PREVIEW_MAX_CHARS = 200;

    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final CourseRepository courseRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LessonSummaryResponse> listByCourseId(Long courseId, UserPrincipal principal) {
        Course course = findCourseOrThrow(courseId);

        List<Lesson> lessons;
        if (canManage(course, principal)) {
            lessons = lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        } else if (course.getStatus() == CourseStatus.PUBLISHED) {
            lessons = lessonRepository.findByCourseIdAndPublishedTrueOrderByOrderIndexAsc(courseId);
        } else {
            throw new AccessDeniedException("You are not allowed to view lessons of this course");
        }

        return LessonMapper.toSummaryResponses(lessons);
    }

    @Override
    @Transactional(readOnly = true)
    public LessonDetailResponse getById(Long lessonId, UserPrincipal principal) {
        Lesson lesson = findLessonOrThrow(lessonId);
        if (!canView(lesson, principal)) {
            throw new AccessDeniedException("You are not allowed to view this lesson");
        }

        return LessonMapper.toDetailResponse(lesson);
    }

    @Override
    public LessonDetailResponse create(Long courseId, LessonCreateRequest request, UserPrincipal principal) {
        Course course = findCourseOrThrow(courseId);
        assertManagePermission(course, principal);

        if (lessonRepository.existsByCourseIdAndOrderIndex(courseId, request.orderIndex())) {
            throw new ConflictException("Order index already exists in this course: " + request.orderIndex());
        }

        Lesson lesson = new Lesson();
        lesson.setCourse(course);
        lesson.setTitle(request.title());
        lesson.setContentUrl(request.contentUrl());
        lesson.setTextContent(request.textContent());
        lesson.setOrderIndex(request.orderIndex());
        lesson.setPublished(false);

        Lesson saved = lessonRepository.save(lesson);
        log.info("Lesson created: id={}, courseId={}", saved.getId(), courseId);
        return LessonMapper.toDetailResponse(saved);
    }

    @Override
    public LessonDetailResponse update(Long lessonId, LessonUpdateRequest request, UserPrincipal principal) {
        Lesson lesson = findLessonOrThrow(lessonId);
        Course course = lesson.getCourse();
        assertManagePermission(course, principal);

        if (request.orderIndex() != null
                && !request.orderIndex().equals(lesson.getOrderIndex())
                && lessonRepository.existsByCourseIdAndOrderIndexAndIdNot(course.getId(), request.orderIndex(), lessonId)) {
            throw new ConflictException("Order index already exists in this course: " + request.orderIndex());
        }

        if (request.title() != null) {
            lesson.setTitle(request.title());
        }
        if (request.contentUrl() != null) {
            lesson.setContentUrl(request.contentUrl());
        }
        if (request.textContent() != null) {
            lesson.setTextContent(request.textContent());
        }
        if (request.orderIndex() != null) {
            lesson.setOrderIndex(request.orderIndex());
        }

        Lesson saved = lessonRepository.save(lesson);
        log.info("Lesson updated: id={}", saved.getId());
        return LessonMapper.toDetailResponse(saved);
    }

    @Override
    public LessonDetailResponse updatePublishStatus(Long lessonId, LessonPublishRequest request, UserPrincipal principal) {
        Lesson lesson = findLessonOrThrow(lessonId);
        assertManagePermission(lesson.getCourse(), principal);

        lesson.setPublished(request.published());
        Lesson saved = lessonRepository.save(lesson);
        log.info("Lesson publish status updated: id={}, published={}", saved.getId(), saved.isPublished());
        return LessonMapper.toDetailResponse(saved);
    }

    @Override
    public void delete(Long lessonId, UserPrincipal principal) {
        Lesson lesson = findLessonOrThrow(lessonId);
        assertManagePermission(lesson.getCourse(), principal);

        if (lessonProgressRepository.existsByLessonId(lessonId)) {
            throw new ConflictException("Cannot delete lesson because learning progress already exists");
        }

        lessonRepository.delete(lesson);
        log.info("Lesson deleted: id={}", lessonId);
    }

    @Override
    @Transactional(readOnly = true)
    public LessonContentPreviewResponse getContentPreview(Long lessonId, UserPrincipal principal) {
        Lesson lesson = findLessonOrThrow(lessonId);
        if (!canView(lesson, principal)) {
            throw new AccessDeniedException("You are not allowed to preview this lesson");
        }

        return LessonMapper.toContentPreviewResponse(lesson, PREVIEW_MAX_CHARS);
    }

    private Course findCourseOrThrow(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + id));
    }

    private Lesson findLessonOrThrow(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found with id: " + id));
    }

    private boolean canManage(Course course, UserPrincipal principal) {
        Role role = principal.getRole();
        return role == Role.ADMIN
                || (role == Role.TEACHER && course.getTeacher().getId().equals(principal.getUserId()));
    }

    private boolean canView(Lesson lesson, UserPrincipal principal) {
        Course course = lesson.getCourse();
        if (canManage(course, principal)) {
            return true;
        }
        return course.getStatus() == CourseStatus.PUBLISHED && lesson.isPublished();
    }

    private void assertManagePermission(Course course, UserPrincipal principal) {
        if (!canManage(course, principal)) {
            throw new AccessDeniedException("You are not allowed to manage lessons of this course");
        }
    }
}
