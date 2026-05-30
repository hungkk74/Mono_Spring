package com.monowear.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Tên danh mục không được để trống")
        @Size(max = 255, message = "Tên danh mục tối đa 255 ký tự")
        String name,

        String description,

        Long parentId
) {
}
