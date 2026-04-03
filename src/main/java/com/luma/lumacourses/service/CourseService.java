package com.luma.lumacourses.service;

import com.luma.lumacourses.dto.course.CourseCreateRequest;
import com.luma.lumacourses.dto.course.CourseDetailResponse;
import com.luma.lumacourses.dto.course.CourseResponse;
import com.luma.lumacourses.dto.course.CourseStatusUpdateRequest;
import com.luma.lumacourses.dto.course.CourseUpdateRequest;
import com.luma.lumacourses.security.principal.UserPrincipal;
import com.luma.lumacourses.util.enums.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseService {

    Page<CourseResponse> listCourses(String search,
                                     Long teacherId,
                                     CourseStatus status,
                                     Pageable pageable,
                                     UserPrincipal principal);

    CourseDetailResponse getCourseById(Long id, UserPrincipal principal);

    CourseResponse createCourse(CourseCreateRequest request);

    CourseResponse updateCourse(Long id, CourseUpdateRequest request);

    CourseResponse updateStatus(Long id, CourseStatusUpdateRequest request);

    void deleteCourse(Long id);
}
