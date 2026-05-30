package com.monowear.dto.order;

import java.math.BigDecimal;
import java.util.List;

public record RevenueResponse(
        BigDecimal totalRevenue,
        long totalOrders,
        BigDecimal avgRevenuePerDay,
        List<DailyRevenue> dailyRevenue
) {
    public record DailyRevenue(
            String date,
            BigDecimal revenue,
            long orderCount
    ) {}
}
