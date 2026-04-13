package com.luma.lumacourses.mapper;

import com.luma.lumacourses.dto.enrollment.EnrollmentDetailResponse;
import com.luma.lumacourses.dto.enrollment.EnrollmentSummaryResponse;
import com.luma.lumacourses.dto.enrollment.LessonProgressResponse;
import com.luma.lumacourses.entity.Enrollment;
import com.luma.lumacourses.entity.Lesson;
import com.luma.lumacourses.entity.LessonProgress;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class EnrollmentMapper {

    private EnrollmentMapper() {
    }

    public static EnrollmentSummaryResponse toSummaryResponse(Enrollment enrollment) {
        return new EnrollmentSummaryResponse(
                enrollment.getId(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getTitle(),
                enrollment.getCourse().getTeacher().getId(),
                enrollment.getCourse().getTeacher().getFullName(),
                enrollment.getStatus(),
                enrollment.getProgressPercentage(),
                enrollment.getEnrollmentDate(),
                enrollment.getCompletionDate());
    }

    /**

     *
     * @param enrollment
     * @param publishedLessons
     * @param progresses
     */
    public static EnrollmentDetailResponse toDetailResponse(
            Enrollment enrollment,
            List<Lesson> publishedLessons,
            List<LessonProgress> progresses,
            long completedCount) {


        Map<Long, LessonProgress> progressByLessonId = progresses.stream()
                .collect(Collectors.toMap(
                        lp -> lp.getLesson().getId(),
                        Function.identity()));

        List<LessonProgressResponse> lessonResponses = publishedLessons.stream()
                .map(lesson -> {
                    LessonProgress lp = progressByLessonId.get(lesson.getId());
                    return new LessonProgressResponse(
                            lesson.getId(),
                            lesson.getTitle(),
                            lesson.getOrderIndex(),
                            lesson.isPublished(),
                            lp != null && lp.isCompleted(),
                            lp != null ? lp.getCompletedAt() : null,
                            lp != null ? lp.getLastAccessedAt() : null);
                })
                .toList();

        return new EnrollmentDetailResponse(
                enrollment.getId(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getTitle(),
                enrollment.getCourse().getTeacher().getId(),
                enrollment.getCourse().getTeacher().getFullName(),
                enrollment.getStatus(),
                enrollment.getProgressPercentage(),
                publishedLessons.size(),
                (int) completedCount,
                enrollment.getEnrollmentDate(),
                enrollment.getCompletionDate(),
                lessonResponses);
    }
}
