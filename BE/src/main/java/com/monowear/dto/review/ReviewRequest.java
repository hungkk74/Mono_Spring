package com.monowear.dto.review;

import jakarta.validation.constraints.*;

public record ReviewRequest(
        @NotNull(message = "Product ID không được để trống")
        Long productId,

        @NotNull(message = "Rating không được để trống")
        @Min(value = 1, message = "Rating tối thiểu là 1")
        @Max(value = 5, message = "Rating tối đa là 5")
        Integer rating,

        @Size(max = 1000, message = "Bình luận tối đa 1000 ký tự")
        String comment
) {
}
