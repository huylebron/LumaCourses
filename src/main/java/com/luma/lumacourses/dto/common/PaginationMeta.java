package com.luma.lumacourses.dto.common;

public record PaginationMeta(
        int currentPage,
        int pageSize,
        int totalPages,
        long totalItems
) {
}

