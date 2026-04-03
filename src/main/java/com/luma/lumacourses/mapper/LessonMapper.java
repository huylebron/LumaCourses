package com.luma.lumacourses.mapper;

import com.luma.lumacourses.dto.course.LessonSummaryResponse;
import com.luma.lumacourses.entity.Lesson;

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
}
