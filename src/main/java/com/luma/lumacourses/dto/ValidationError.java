package com.luma.lumacourses.dto;

public record ValidationError(
        String field,
        String message
) {
}

