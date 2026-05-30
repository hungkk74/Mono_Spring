package com.monowear.dto.promotion;

import java.math.BigDecimal;

public record CouponApplyResponse(
        String code,
        String description,
        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount
) {
}
