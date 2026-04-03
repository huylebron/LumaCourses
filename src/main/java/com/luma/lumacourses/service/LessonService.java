package com.luma.lumacourses.service;

import com.luma.lumacourses.dto.course.LessonSummaryResponse;
import com.luma.lumacourses.dto.lesson.LessonContentPreviewResponse;
import com.luma.lumacourses.dto.lesson.LessonCreateRequest;
import com.luma.lumacourses.dto.lesson.LessonDetailResponse;
import com.luma.lumacourses.dto.lesson.LessonPublishRequest;
import com.luma.lumacourses.dto.lesson.LessonUpdateRequest;
import com.luma.lumacourses.security.principal.UserPrincipal;

import java.util.List;

public interface LessonService {

    List<LessonSummaryResponse> listByCourseId(Long courseId, UserPrincipal principal);

    LessonDetailResponse getById(Long lessonId, UserPrincipal principal);

    LessonDetailResponse create(Long courseId, LessonCreateRequest request, UserPrincipal principal);

    LessonDetailResponse update(Long lessonId, LessonUpdateRequest request, UserPrincipal principal);

    LessonDetailResponse updatePublishStatus(Long lessonId, LessonPublishRequest request, UserPrincipal principal);

    void delete(Long lessonId, UserPrincipal principal);

    LessonContentPreviewResponse getContentPreview(Long lessonId, UserPrincipal principal);
}
