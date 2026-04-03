package com.luma.lumacourses.service.impl;

import com.luma.lumacourses.dto.course.CourseCreateRequest;
import com.luma.lumacourses.dto.course.CourseDetailResponse;
import com.luma.lumacourses.dto.course.CourseResponse;
import com.luma.lumacourses.dto.course.CourseStatusUpdateRequest;
import com.luma.lumacourses.dto.course.CourseUpdateRequest;
import com.luma.lumacourses.dto.course.LessonSummaryResponse;
import com.luma.lumacourses.entity.Course;
import com.luma.lumacourses.entity.Lesson;
import com.luma.lumacourses.entity.User;
import com.luma.lumacourses.mapper.CourseMapper;
import com.luma.lumacourses.mapper.LessonMapper;
import com.luma.lumacourses.repository.CourseRepository;
import com.luma.lumacourses.repository.LessonRepository;
import com.luma.lumacourses.repository.UserRepository;
import com.luma.lumacourses.security.principal.UserPrincipal;
import com.luma.lumacourses.service.CourseService;
import com.luma.lumacourses.util.enums.CourseStatus;
import com.luma.lumacourses.util.enums.Role;
import com.luma.lumacourses.util.exception.ConflictException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;


    // List courses
    @Override
    @Transactional(readOnly = true)
    public Page<CourseResponse> listCourses(String search,
                                            Long teacherId,
                                            CourseStatus status,
                                            Pageable pageable,
                                            UserPrincipal principal) {
        String normalizedSearch = StringUtils.hasText(search) ? search.trim() : null;

        return courseRepository.findVisibleCourses(
                        normalizedSearch,
                        teacherId,
                        status,
                        principal.getRole(),
                        principal.getUserId(),
                        pageable)
                .map(CourseMapper::toResponse);
    }

    // get course by id ( detail)

    @Override
    @Transactional(readOnly = true)
    public CourseDetailResponse getCourseById(Long id, UserPrincipal principal) {
        Course course = findCourseOrThrow(id);
        if (!canViewCourse(course, principal)) {
            throw new AccessDeniedException("You are not allowed to view this course");
        }

        List<Lesson> lessons = lessonRepository.findByCourseIdAndPublishedTrueOrderByOrderIndexAsc(id);
        List<LessonSummaryResponse> lessonResponses = LessonMapper.toSummaryResponses(lessons);
        return CourseMapper.toDetailResponse(course, lessonResponses);
    }


    @Override
    public CourseResponse createCourse(CourseCreateRequest request) {
        User teacher = findTeacherOrThrow(request.teacherId());

        Course course = new Course();
        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setTeacher(teacher);
        course.setPrice(request.price());
        course.setDurationHours(request.durationHours());
        course.setStatus(CourseStatus.DRAFT);

        Course saved = courseRepository.save(course);
        log.info("Course created: id={}, teacherId={}", saved.getId(), teacher.getId());
        return CourseMapper.toResponse(saved);
    }



    @Override
    public CourseResponse updateCourse(Long id, CourseUpdateRequest request) {
        Course course = findCourseOrThrow(id);

        if (request.title() != null) {
            course.setTitle(request.title());
        }
        if (request.description() != null) {
            course.setDescription(request.description());
        }
        if (request.teacherId() != null && !request.teacherId().equals(course.getTeacher().getId())) {
            course.setTeacher(findTeacherOrThrow(request.teacherId()));
        }
        if (request.price() != null) {
            course.setPrice(request.price());
        }
        if (request.durationHours() != null) {
            course.setDurationHours(request.durationHours());
        }

        Course saved = courseRepository.save(course);
        log.info("Course updated: id={}", saved.getId());
        return CourseMapper.toResponse(saved);
    }

    // update course status
    @Override
    public CourseResponse updateStatus(Long id, CourseStatusUpdateRequest request) {
        Course course = findCourseOrThrow(id);
        course.setStatus(request.status());

        Course saved = courseRepository.save(course);
        log.info("Course status updated: id={}, status={}", saved.getId(), saved.getStatus());
        return CourseMapper.toResponse(saved);
    }

    // delete course
    @Override
    public void deleteCourse(Long id) {
        Course course = findCourseOrThrow(id);
        course.setStatus(CourseStatus.ARCHIVED);
        courseRepository.save(course);
        log.info("Course archived: id={}", id);
    }

    // helper
    private Course findCourseOrThrow(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + id));
    }



    private User findTeacherOrThrow(Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found with id: " + teacherId));

        if (teacher.getRole() != Role.TEACHER) {
            throw new ConflictException("User with id " + teacherId + " is not a TEACHER");
        }
        if (!teacher.isActive()) {
            throw new ConflictException("Teacher with id " + teacherId + " is inactive");
        }
        return teacher;
    }

    // helper ( auth )
    private boolean canViewCourse(Course course, UserPrincipal principal) {
        return switch (principal.getRole()) {
            case ADMIN -> true;
            case STUDENT -> course.getStatus() == CourseStatus.PUBLISHED;
            case TEACHER -> course.getStatus() == CourseStatus.PUBLISHED
                    || course.getTeacher().getId().equals(principal.getUserId());
        };
    }
}
