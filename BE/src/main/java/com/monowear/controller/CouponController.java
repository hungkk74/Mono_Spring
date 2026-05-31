package com.monowear.controller;

import com.monowear.dto.common.ApiResponse;
import com.monowear.dto.promotion.CouponApplyResponse;
import com.monowear.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/coupons")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<CouponApplyResponse>> validate(
            @RequestParam String code,
            @RequestParam BigDecimal subtotalAmount) {
        return ResponseEntity.ok(ApiResponse.success(couponService.validate(code, subtotalAmount)));
    }
}
