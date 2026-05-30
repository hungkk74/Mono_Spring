package com.monowear.dto.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewReplyRequest(
        @NotNull(message = "Review ID không được để trống")
        Long reviewId,

        @NotBlank(message = "Nội dung phản hồi không được để trống")
        @Size(max = 500, message = "Phản hồi tối đa 500 ký tự")
        String content
) {
}
