package com.luma.lumacourses.service;

import com.luma.lumacourses.dto.review.ReviewCreateRequest;
import com.luma.lumacourses.dto.review.ReviewResponse;
import com.luma.lumacourses.dto.review.ReviewUpdateRequest;
import com.luma.lumacourses.security.principal.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    Page<ReviewResponse> listCourseReviews(Long courseId, Pageable pageable, UserPrincipal principal);

    ReviewResponse createReview(Long courseId, ReviewCreateRequest request, UserPrincipal principal);

    ReviewResponse updateReview(Long reviewId, ReviewUpdateRequest request, UserPrincipal principal);

    void deleteReview(Long reviewId, UserPrincipal principal);
}

