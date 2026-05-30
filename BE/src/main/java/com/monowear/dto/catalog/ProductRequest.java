package com.monowear.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductRequest(
        @NotNull(message = "ID danh mục không được để trống")
        Long categoryId,

        @NotBlank(message = "Tên sản phẩm không được để trống")
        @Size(max = 255, message = "Tên sản phẩm tối đa 255 ký tự")
        String name,

        @Size(max = 255, message = "Chất liệu tối đa 255 ký tự")
        String material,

        String description,

        @Size(max = 500, message = "URL ảnh tối đa 500 ký tự")
        String imageUrl
) {
}
