package com.luma.lumacourses.dto.common;

public record ValidationError(
        String field,
        String message
) {
}

