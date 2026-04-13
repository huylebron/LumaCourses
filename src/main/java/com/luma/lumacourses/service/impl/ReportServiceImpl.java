package com.luma.lumacourses.service.impl;

import com.luma.lumacourses.dto.report.StudentCourseProgressResponse;
import com.luma.lumacourses.dto.report.StudentProgressReportResponse;
import com.luma.lumacourses.dto.report.TeacherCourseOverviewItemResponse;
import com.luma.lumacourses.dto.report.TeacherCoursesOverviewResponse;
import com.luma.lumacourses.dto.report.TopCourseReportResponse;
import com.luma.lumacourses.entity.Course;
import com.luma.lumacourses.entity.Enrollment;
import com.luma.lumacourses.entity.User;
import com.luma.lumacourses.mapper.ReportMapper;
import com.luma.lumacourses.repository.CourseRepository;
import com.luma.lumacourses.repository.EnrollmentRepository;
import com.luma.lumacourses.repository.ReviewRepository;
import com.luma.lumacourses.repository.UserRepository;
import com.luma.lumacourses.service.ReportService;
import com.luma.lumacourses.util.enums.CourseStatus;
import com.luma.lumacourses.util.enums.EnrollmentStatus;
import com.luma.lumacourses.util.enums.Role;
import com.luma.lumacourses.util.exception.ConflictException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    @Override
    public List<TopCourseReportResponse> getTopCourses(int limit) {
        return courseRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(course -> ReportMapper.toTopCourseReportResponse(
                        course,
                        enrollmentRepository.countByCourseId(course.getId())))
                .sorted(Comparator
                        .comparingLong(TopCourseReportResponse::enrollmentCount)
                        .reversed())
                .limit(limit)
                .toList();
    }

    @Override
    public StudentProgressReportResponse getStudentProgress(Long studentId) {
        User student = findStudentOrThrow(studentId);
        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdOrderByEnrollmentDateDesc(studentId);

        long totalEnrollments = enrollments.size();
        long completedEnrollments = enrollments.stream()
                .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.COMPLETED)
                .count();

        BigDecimal averageProgress = calculateAverageProgress(enrollments, totalEnrollments);

        List<StudentCourseProgressResponse> courseProgresses = enrollments.stream()
                .sorted(Comparator.comparing(Enrollment::getEnrollmentDate).reversed())
                .map(ReportMapper::toStudentCourseProgressResponse)
                .toList();

        return new StudentProgressReportResponse(
                student.getId(),
                student.getFullName(),
                student.getEmail(),
                totalEnrollments,
                completedEnrollments,
                averageProgress,
                courseProgresses);
    }

    @Override
    public TeacherCoursesOverviewResponse getTeacherCoursesOverview(Long teacherId) {
        User teacher = findTeacherOrThrow(teacherId);
        List<Course> courses = courseRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId);

        long totalCourses = courses.size();
        long publishedCourses = courses.stream().filter(c -> c.getStatus() == CourseStatus.PUBLISHED).count();
        long draftCourses = courses.stream().filter(c -> c.getStatus() == CourseStatus.DRAFT).count();
        long archivedCourses = courses.stream().filter(c -> c.getStatus() == CourseStatus.ARCHIVED).count();

        long totalEnrollments = enrollmentRepository.countByCourseTeacherId(teacherId);
        long totalReviews = reviewRepository.countByCourseTeacherId(teacherId);
        BigDecimal averageRating = ReportMapper.toScaledDecimal(reviewRepository.findAverageRatingByTeacherId(teacherId));

        List<TeacherCourseOverviewItemResponse> courseOverviews = courses.stream()
                .map(course -> {
                    Long courseId = course.getId();
                    long enrollmentCount = enrollmentRepository.countByCourseId(courseId);
                    long reviewCount = reviewRepository.countByCourseId(courseId);
                    Double averageCourseRating = reviewRepository.findAverageRatingByCourseId(courseId);

                    return ReportMapper.toTeacherCourseOverviewItemResponse(
                            course,
                            enrollmentCount,
                            reviewCount,
                            averageCourseRating);
                })
                .toList();

        log.info("Teacher overview report generated: teacherId={}, totalCourses={}", teacherId, totalCourses);
        return new TeacherCoursesOverviewResponse(
                teacher.getId(),
                teacher.getFullName(),
                teacher.getEmail(),
                totalCourses,
                publishedCourses,
                draftCourses,
                archivedCourses,
                totalEnrollments,
                totalReviews,
                averageRating,
                courseOverviews);
    }

    private User findStudentOrThrow(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + studentId));
        if (student.getRole() != Role.STUDENT) {
            throw new ConflictException("User with id " + studentId + " is not a STUDENT");
        }
        return student;
    }

    private User findTeacherOrThrow(Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found with id: " + teacherId));
        if (teacher.getRole() != Role.TEACHER) {
            throw new ConflictException("User with id " + teacherId + " is not a TEACHER");
        }
        return teacher;
    }

    private BigDecimal calculateAverageProgress(List<Enrollment> enrollments, long totalEnrollments) {
        if (totalEnrollments == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal totalProgress = enrollments.stream()
                .map(Enrollment::getProgressPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalProgress
                .divide(BigDecimal.valueOf(totalEnrollments), 2, RoundingMode.HALF_UP);
    }
}
