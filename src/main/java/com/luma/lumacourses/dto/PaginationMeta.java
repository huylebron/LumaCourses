package com.luma.lumacourses.dto;

public record PaginationMeta(
        int currentPage,
        int pageSize,
        int totalPages,
        long totalItems
) {
}

