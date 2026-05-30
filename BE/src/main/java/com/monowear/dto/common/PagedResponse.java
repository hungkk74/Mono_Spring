package com.monowear.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * Response chuẩn cho các API phân trang.
 *
 * @param content     Danh sách item của trang hiện tại
 * @param page        Trang hiện tại (0-indexed)
 * @param size        Số item mỗi trang
 * @param totalItems  Tổng số item
 * @param totalPages  Tổng số trang
 */
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
