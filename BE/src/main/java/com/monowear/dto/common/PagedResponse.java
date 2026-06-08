package com.monowear.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalItems,
        int totalPages
) {
    public static <T> PagedResponse<T> of(List<T> content, int page, int size, long totalItems) {
        if (size <= 0) size = 20;
        int totalPages = (int) Math.ceil((double) totalItems / size);
        return new PagedResponse<>(content, page, size, totalItems, totalPages);
    }
}
