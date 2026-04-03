package com.luma.lumacourses.mapper;

import com.luma.lumacourses.dto.course.LessonSummaryResponse;
import com.luma.lumacourses.dto.lesson.LessonContentPreviewResponse;
import com.luma.lumacourses.dto.lesson.LessonDetailResponse;
import com.luma.lumacourses.entity.Lesson;
import org.springframework.util.StringUtils;

import java.util.List;

public final class LessonMapper {

    private LessonMapper() {
    }

    public static LessonSummaryResponse toSummaryResponse(Lesson lesson) {
        return new LessonSummaryResponse(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getOrderIndex(),
                lesson.isPublished());
    }

    public static List<LessonSummaryResponse> toSummaryResponses(List<Lesson> lessons) {
        return lessons.stream().map(LessonMapper::toSummaryResponse).toList();
    }

    public static LessonDetailResponse toDetailResponse(Lesson lesson) {
        return new LessonDetailResponse(
                lesson.getId(),
                lesson.getCourse().getId(),
                lesson.getTitle(),
                lesson.getContentUrl(),
                lesson.getTextContent(),
                lesson.getOrderIndex(),
                lesson.isPublished(),
                lesson.getCreatedAt(),
                lesson.getUpdatedAt());
    }

    public static LessonContentPreviewResponse toContentPreviewResponse(Lesson lesson, int maxChars) {
        String original = lesson.getTextContent();
        String normalized = StringUtils.hasText(original) ? original.trim() : "";
        boolean hasMore = normalized.length() > maxChars;
        String excerpt = hasMore ? normalized.substring(0, maxChars) : normalized;

        return new LessonContentPreviewResponse(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getContentUrl(),
                excerpt,
                hasMore,
                lesson.getUpdatedAt());
    }
}
