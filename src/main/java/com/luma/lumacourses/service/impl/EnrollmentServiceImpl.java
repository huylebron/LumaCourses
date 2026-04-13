package com.luma.lumacourses.service.impl;

import com.luma.lumacourses.dto.enrollment.EnrollmentCreateRequest;
import com.luma.lumacourses.dto.enrollment.EnrollmentDetailResponse;
import com.luma.lumacourses.dto.enrollment.EnrollmentSummaryResponse;
import com.luma.lumacourses.entity.Course;
import com.luma.lumacourses.entity.Enrollment;
import com.luma.lumacourses.entity.Lesson;
import com.luma.lumacourses.entity.LessonProgress;
import com.luma.lumacourses.entity.User;
import com.luma.lumacourses.mapper.EnrollmentMapper;
import com.luma.lumacourses.repository.CourseRepository;
import com.luma.lumacourses.repository.EnrollmentRepository;
import com.luma.lumacourses.repository.LessonProgressRepository;
import com.luma.lumacourses.repository.LessonRepository;
import com.luma.lumacourses.repository.UserRepository;
import com.luma.lumacourses.security.principal.UserPrincipal;
import com.luma.lumacourses.service.EnrollmentService;
import com.luma.lumacourses.util.enums.CourseStatus;
import com.luma.lumacourses.util.enums.EnrollmentStatus;
import com.luma.lumacourses.util.exception.ConflictException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final UserRepository userRepository;

 // list
    @Override
    @Transactional(readOnly = true)
    public Page<EnrollmentSummaryResponse> listMyEnrollments(UserPrincipal principal, Pageable pageable) {
        return enrollmentRepository
                .findByStudentId(principal.getUserId(), pageable)
                .map(EnrollmentMapper::toSummaryResponse);
    }


    // Create


    @Override
    public EnrollmentDetailResponse createEnrollment(EnrollmentCreateRequest request, UserPrincipal principal) {
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + request.courseId()));

        // Course  PUBLISHED
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new ConflictException("Cannot enroll in a course that is not published");
        }

        // Duplicate enrollment check
        if (enrollmentRepository.existsByStudentIdAndCourseId(principal.getUserId(), course.getId())) {
            throw new ConflictException("Already enrolled in course with id: " + course.getId());
        }

        User student = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + principal.getUserId()));

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        enrollment.setProgressPercentage(BigDecimal.ZERO);
        enrollment.setCompletionDate(null);

        Enrollment saved = enrollmentRepository.save(enrollment);
        log.info("Enrollment created: id={}, studentId={}, courseId={}", saved.getId(), student.getId(),
                course.getId());

        return buildDetailResponse(saved);
    }


    // Get detail

    @Override
    @Transactional(readOnly = true)
    public EnrollmentDetailResponse getMyEnrollment(Long enrollmentId, UserPrincipal principal) {
        Enrollment enrollment = findOwnedEnrollmentOrThrow(enrollmentId, principal.getUserId());
        return buildDetailResponse(enrollment);
    }


    // Complete lesson (idempotent)

    @Override
    public EnrollmentDetailResponse completeLesson(Long enrollmentId, Long lessonId, UserPrincipal principal) {
        Enrollment enrollment = findOwnedEnrollmentOrThrow(enrollmentId, principal.getUserId());

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found with id: " + lessonId));


        if (!lesson.getCourse().getId().equals(enrollment.getCourse().getId())) {
            throw new ConflictException("Lesson " + lessonId + " does not belong to the enrolled course");
        }

        // Lesson published
        if (!lesson.isPublished()) {
            throw new ConflictException("Lesson " + lessonId + " is not published");
        }


        LessonProgress progress = lessonProgressRepository
                .findByEnrollmentIdAndLessonId(enrollmentId, lessonId)
                .orElseGet(() -> {
                    LessonProgress lp = new LessonProgress();
                    lp.setEnrollment(enrollment);
                    lp.setLesson(lesson);
                    return lp;
                });

        if (!progress.isCompleted()) {
            progress.setCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
            lessonProgressRepository.save(progress);
            log.info("LessonProgress marked completed: enrollmentId={}, lessonId={}", enrollmentId, lessonId);
        }

        recalculateProgress(enrollment);
        Enrollment saved = enrollmentRepository.save(enrollment);

        return buildDetailResponse(saved);
    }


  // helpers



    private void recalculateProgress(Enrollment enrollment) {
        long totalPublished = lessonRepository.countByCourseIdAndPublishedTrue(enrollment.getCourse().getId());
        long completedPublished = lessonProgressRepository.countByEnrollmentIdAndCompletedTrue(enrollment.getId());

        BigDecimal percentage;
        if (totalPublished == 0) {
            percentage = BigDecimal.ZERO;
        } else {
            percentage = BigDecimal.valueOf(completedPublished)
                    .divide(BigDecimal.valueOf(totalPublished), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        enrollment.setProgressPercentage(percentage);

        if (percentage.compareTo(BigDecimal.valueOf(100)) >= 0) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
            if (enrollment.getCompletionDate() == null) {
                enrollment.setCompletionDate(LocalDateTime.now());
            }
        } else {
            enrollment.setStatus(EnrollmentStatus.ENROLLED);
            enrollment.setCompletionDate(null);
        }

        log.info("Progress recalculated: enrollmentId={}, progress={}%, status={}",
                enrollment.getId(), percentage, enrollment.getStatus());
    }


    private EnrollmentDetailResponse buildDetailResponse(Enrollment enrollment) {
        List<Lesson> publishedLessons = lessonRepository
                .findByCourseIdAndPublishedTrueOrderByOrderIndexAsc(enrollment.getCourse().getId());
        List<LessonProgress> progresses = lessonProgressRepository
                .findByEnrollmentId(enrollment.getId());
        long completedCount = lessonProgressRepository
                .countByEnrollmentIdAndCompletedTrue(enrollment.getId());

        return EnrollmentMapper.toDetailResponse(enrollment, publishedLessons, progresses, completedCount);
    }

    private Enrollment findOwnedEnrollmentOrThrow(Long enrollmentId, Long studentId) {
        return enrollmentRepository.findByIdAndStudentId(enrollmentId, studentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Enrollment not found with id: " + enrollmentId));
    }
}
