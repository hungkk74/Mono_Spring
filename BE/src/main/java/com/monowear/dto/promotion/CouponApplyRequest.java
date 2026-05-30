package com.monowear.dto.promotion;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CouponApplyRequest(
        @NotBlank(message = "Mã giảm giá không được để trống")
        String code,

        @NotNull(message = "Tạm tính không được để trống")
        @DecimalMin(value = "0.01", message = "Tạm tính phải lớn hơn 0")
        BigDecimal subtotalAmount
) {
}
