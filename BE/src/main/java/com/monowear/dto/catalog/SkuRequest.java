package com.monowear.dto.catalog;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record SkuRequest(
        @NotNull(message = "ID sản phẩm không được để trống")
        Long productId,

        @NotBlank(message = "Mã SKU không được để trống")
        @Size(max = 100, message = "Mã SKU tối đa 100 ký tự")
        String skuCode,

        @NotBlank(message = "Size không được để trống")
        @Size(max = 20, message = "Size tối đa 20 ký tự")
        String size,

        @NotBlank(message = "Màu sắc không được để trống")
        @Size(max = 50, message = "Màu sắc tối đa 50 ký tự")
        String color,

        @NotNull(message = "Giá không được để trống")
        @DecimalMin(value = "0.01", message = "Giá phải lớn hơn 0")
        BigDecimal price,

        @NotNull(message = "Tồn kho không được để trống")
        @Min(value = 0, message = "Tồn kho không được âm")
        Integer stock
) {
}
