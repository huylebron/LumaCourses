package com.luma.lumacourses.dto.course;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.luma.lumacourses.util.enums.CourseStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CourseResponse(
        Long id,
        String title,
        String description,
        CourseTeacherResponse teacher,
        BigDecimal price,
        Integer durationHours,
        CourseStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
