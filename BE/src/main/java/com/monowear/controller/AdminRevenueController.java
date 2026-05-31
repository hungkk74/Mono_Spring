package com.monowear.controller;

import com.monowear.dto.common.ApiResponse;
import com.monowear.dto.order.RevenueResponse;
import com.monowear.service.RevenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/revenue")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminRevenueController {

    private final RevenueService revenueService;

    @GetMapping
    public ResponseEntity<ApiResponse<RevenueResponse>> getRevenue(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        
        if (from == null || to == null) {
            to = LocalDate.now();
            if ("today".equalsIgnoreCase(period)) {
                from = to;
            } else if ("7d".equalsIgnoreCase(period)) {
                from = to.minusDays(6);
            } else { // default to 30d
                from = to.minusDays(29);
            }
        }
        
        return ResponseEntity.ok(ApiResponse.success(revenueService.getRevenueSummary(from, to)));
    }
}
