package com.monowear.service;

import com.monowear.dto.promotion.CouponApplyResponse;
import com.monowear.entity.Coupon;
import com.monowear.exception.BadRequestException;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@ApplicationScoped
public class CouponService {

    public CouponApplyResponse validate(String rawCode, BigDecimal subtotalAmount) {
        Coupon coupon = findUsableCoupon(rawCode, subtotalAmount);
        BigDecimal discount = calculateDiscount(coupon, subtotalAmount);
        return new CouponApplyResponse(
                coupon.code,
                coupon.description,
                subtotalAmount,
                discount,
                subtotalAmount.subtract(discount).max(BigDecimal.ZERO)
        );
    }

    public CouponApplyResponse applyAndConsume(String rawCode, BigDecimal subtotalAmount) {
        Coupon coupon = findUsableCoupon(rawCode, subtotalAmount);
        BigDecimal discount = calculateDiscount(coupon, subtotalAmount);
        // Atomic increment — tránh race condition khi nhiều user dùng cùng lúc
        int updated = Coupon.update("usedCount = COALESCE(usedCount, 0) + 1 WHERE id = ?1", coupon.id);
        if (updated == 0) {
            throw new BadRequestException("Mã giảm giá không thể áp dụng lúc này", "COUPON_UPDATE_FAILED");
        }
        return new CouponApplyResponse(
                coupon.code,
                coupon.description,
                subtotalAmount,
                discount,
                subtotalAmount.subtract(discount).max(BigDecimal.ZERO)
        );
    }

    private Coupon findUsableCoupon(String rawCode, BigDecimal subtotalAmount) {
        if (rawCode == null || rawCode.isBlank()) {
            throw new BadRequestException("Vui lòng nhập mã giảm giá", "COUPON_REQUIRED");
        }

        Coupon coupon = Coupon.findByCode(rawCode)
                .orElseThrow(() -> new BadRequestException("Mã giảm giá không tồn tại", "COUPON_NOT_FOUND"));

        LocalDateTime now = LocalDateTime.now();
        if (!Boolean.TRUE.equals(coupon.isActive)) {
            throw new BadRequestException("Mã giảm giá đã ngừng áp dụng", "COUPON_INACTIVE");
        }
        if (coupon.startAt != null && coupon.startAt.isAfter(now)) {
            throw new BadRequestException("Mã giảm giá chưa đến thời gian áp dụng", "COUPON_NOT_STARTED");
        }
        if (coupon.endAt != null && coupon.endAt.isBefore(now)) {
            throw new BadRequestException("Mã giảm giá đã hết hạn", "COUPON_EXPIRED");
        }
        if (coupon.usageLimit != null && coupon.usedCount != null && coupon.usedCount >= coupon.usageLimit) {
            throw new BadRequestException("Mã giảm giá đã hết lượt sử dụng", "COUPON_USAGE_LIMIT");
        }
        if (coupon.minOrderAmount != null && subtotalAmount.compareTo(coupon.minOrderAmount) < 0) {
            throw new BadRequestException(
                    "Đơn hàng chưa đạt giá trị tối thiểu để dùng mã này",
                    "COUPON_MIN_ORDER"
            );
        }
        return coupon;
    }

    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal subtotalAmount) {
        BigDecimal discount;
        if ("PERCENT".equalsIgnoreCase(coupon.discountType)) {
            discount = subtotalAmount
                    .multiply(coupon.discountValue)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (coupon.maxDiscountAmount != null && discount.compareTo(coupon.maxDiscountAmount) > 0) {
                discount = coupon.maxDiscountAmount;
            }
        } else if ("FIXED".equalsIgnoreCase(coupon.discountType)) {
            discount = coupon.discountValue;
        } else {
            throw new BadRequestException("Loại mã giảm giá không hợp lệ", "COUPON_INVALID_TYPE");
        }
        return discount.min(subtotalAmount).max(BigDecimal.ZERO);
    }
}
