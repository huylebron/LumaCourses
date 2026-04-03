package com.luma.lumacourses.dto.common;

import java.util.List;

public record PagedData<T>(
        List<T> items,
        PaginationMeta pagination
) {
}

