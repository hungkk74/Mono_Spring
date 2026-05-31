package com.monowear.service;

import com.monowear.dto.promotion.CouponApplyResponse;
import com.monowear.entity.Coupon;
import com.monowear.exception.BadRequestException;
import com.monowear.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponApplyResponse validate(String rawCode, BigDecimal subtotalAmount) {
        Coupon coupon = findUsableCoupon(rawCode, subtotalAmount);
        BigDecimal discount = calculateDiscount(coupon, subtotalAmount);
        return new CouponApplyResponse(
                coupon.getCode(),
                coupon.getDescription(),
                subtotalAmount,
                discount,
                subtotalAmount.subtract(discount).max(BigDecimal.ZERO)
        );
    }

    @Transactional
    public CouponApplyResponse applyAndConsume(String rawCode, BigDecimal subtotalAmount) {
        Coupon coupon = findUsableCoupon(rawCode, subtotalAmount);
        BigDecimal discount = calculateDiscount(coupon, subtotalAmount);
        int updated = couponRepository.incrementUsedCount(coupon.getId());
        if (updated == 0) {
            throw new BadRequestException("Mã giảm giá không thể áp dụng lúc này", "COUPON_UPDATE_FAILED");
        }
        return new CouponApplyResponse(
                coupon.getCode(),
                coupon.getDescription(),
                subtotalAmount,
                discount,
                subtotalAmount.subtract(discount).max(BigDecimal.ZERO)
        );
    }

    private Coupon findUsableCoupon(String rawCode, BigDecimal subtotalAmount) {
        if (rawCode == null || rawCode.isBlank()) {
            throw new BadRequestException("Vui lòng nhập mã giảm giá", "COUPON_REQUIRED");
        }

        Coupon coupon = couponRepository.findByCodeIgnoreCase(rawCode.trim())
                .orElseThrow(() -> new BadRequestException("Mã giảm giá không tồn tại", "COUPON_NOT_FOUND"));

        LocalDateTime now = LocalDateTime.now();
        if (!Boolean.TRUE.equals(coupon.getIsActive())) {
            throw new BadRequestException("Mã giảm giá đã ngừng áp dụng", "COUPON_INACTIVE");
        }
        if (coupon.getStartAt() != null && coupon.getStartAt().isAfter(now)) {
            throw new BadRequestException("Mã giảm giá chưa đến thời gian áp dụng", "COUPON_NOT_STARTED");
        }
        if (coupon.getEndAt() != null && coupon.getEndAt().isBefore(now)) {
            throw new BadRequestException("Mã giảm giá đã hết hạn", "COUPON_EXPIRED");
        }
        if (coupon.getUsageLimit() != null && coupon.getUsedCount() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new BadRequestException("Mã giảm giá đã hết lượt sử dụng", "COUPON_USAGE_LIMIT");
        }
        if (coupon.getMinOrderAmount() != null && subtotalAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new BadRequestException("Đơn hàng chưa đạt giá trị tối thiểu để dùng mã này", "COUPON_MIN_ORDER");
        }
        return coupon;
    }

    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal subtotalAmount) {
        BigDecimal discount;
        if ("PERCENT".equalsIgnoreCase(coupon.getDiscountType())) {
            discount = subtotalAmount
                    .multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (coupon.getMaxDiscountAmount() != null && discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
                discount = coupon.getMaxDiscountAmount();
            }
        } else if ("FIXED".equalsIgnoreCase(coupon.getDiscountType())) {
            discount = coupon.getDiscountValue();
        } else {
            throw new BadRequestException("Loại mã giảm giá không hợp lệ", "COUPON_INVALID_TYPE");
        }
        return discount.min(subtotalAmount).max(BigDecimal.ZERO);
    }
}
