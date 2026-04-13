package com.luma.lumacourses.service;

import com.luma.lumacourses.dto.enrollment.EnrollmentCreateRequest;
import com.luma.lumacourses.dto.enrollment.EnrollmentDetailResponse;
import com.luma.lumacourses.dto.enrollment.EnrollmentSummaryResponse;
import com.luma.lumacourses.security.principal.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EnrollmentService {

    Page<EnrollmentSummaryResponse> listMyEnrollments(UserPrincipal principal, Pageable pageable);

    EnrollmentDetailResponse createEnrollment(EnrollmentCreateRequest request, UserPrincipal principal);

    EnrollmentDetailResponse getMyEnrollment(Long enrollmentId, UserPrincipal principal);

    EnrollmentDetailResponse completeLesson(Long enrollmentId, Long lessonId, UserPrincipal principal);
}
