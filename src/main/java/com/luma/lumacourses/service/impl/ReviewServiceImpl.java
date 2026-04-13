package com.luma.lumacourses.service.impl;

import com.luma.lumacourses.dto.review.ReviewCreateRequest;
import com.luma.lumacourses.dto.review.ReviewResponse;
import com.luma.lumacourses.dto.review.ReviewUpdateRequest;
import com.luma.lumacourses.entity.Course;
import com.luma.lumacourses.entity.Review;
import com.luma.lumacourses.entity.User;
import com.luma.lumacourses.mapper.ReviewMapper;
import com.luma.lumacourses.repository.CourseRepository;
import com.luma.lumacourses.repository.EnrollmentRepository;
import com.luma.lumacourses.repository.ReviewRepository;
import com.luma.lumacourses.repository.UserRepository;
import com.luma.lumacourses.security.principal.UserPrincipal;
import com.luma.lumacourses.service.ReviewService;
import com.luma.lumacourses.util.enums.CourseStatus;
import com.luma.lumacourses.util.enums.EnrollmentStatus;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> listCourseReviews(Long courseId, Pageable pageable, UserPrincipal principal) {
        Course course = findCourseOrThrow(courseId);
        if (!canViewCourse(course, principal)) {
            throw new AccessDeniedException("You are not allowed to view reviews of this course");
        }

        return reviewRepository.findByCourseId(courseId, pageable)
                .map(ReviewMapper::toResponse);
    }

    @Override
    public ReviewResponse createReview(Long courseId, ReviewCreateRequest request, UserPrincipal principal) {
        if (principal.getRole() != Role.STUDENT) {
            throw new AccessDeniedException("Only STUDENT can create reviews");
        }

        Course course = findCourseOrThrow(courseId);
        Long studentId = principal.getUserId();

        boolean completed = enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(
                studentId,
                courseId,
                EnrollmentStatus.COMPLETED);
        if (!completed) {
            throw new ConflictException("You can only review a course after completing it");
        }

        if (reviewRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            throw new ConflictException("You have already reviewed this course");
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + studentId));

        Review review = new Review();
        review.setCourse(course);
        review.setStudent(student);
        review.setRating(request.rating());
        review.setComment(request.comment());

        Review saved = reviewRepository.save(review);
        log.info("Review created: reviewId={}, courseId={}, studentId={}", saved.getId(), courseId, studentId);
        return ReviewMapper.toResponse(saved);
    }

    @Override
    public ReviewResponse updateReview(Long reviewId, ReviewUpdateRequest request, UserPrincipal principal) {
        Review review = findReviewOrThrow(reviewId);
        assertOwnerOrAdmin(review, principal);

        review.setRating(request.rating());
        review.setComment(request.comment());

        Review saved = reviewRepository.save(review);
        log.info("Review updated: reviewId={}, studentId={}", reviewId, principal.getUserId());
        return ReviewMapper.toResponse(saved);
    }

    @Override
    public void deleteReview(Long reviewId, UserPrincipal principal) {
        Review review = findReviewOrThrow(reviewId);
        assertOwnerOrAdmin(review, principal);

        reviewRepository.delete(review);
        log.info("Review deleted: reviewId={}, studentId={}", reviewId, principal.getUserId());
    }

    private Course findCourseOrThrow(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));
    }

    private Review findReviewOrThrow(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found with id: " + reviewId));
    }

    private boolean canViewCourse(Course course, UserPrincipal principal) {
        return switch (principal.getRole()) {
            case ADMIN -> true;
            case STUDENT -> course.getStatus() == CourseStatus.PUBLISHED;
            case TEACHER -> course.getStatus() == CourseStatus.PUBLISHED
                    || course.getTeacher().getId().equals(principal.getUserId());
        };
    }

    private void assertOwnerOrAdmin(Review review, UserPrincipal principal) {
        boolean isOwner = review.getStudent().getId().equals(principal.getUserId());
        boolean isAdmin = principal.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You are not allowed to modify this review");
        }
    }
}

