package com.monowear.service;

import com.monowear.dto.order.RevenueResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class RevenueService {

    private static final Logger LOG = Logger.getLogger(RevenueService.class);

    @Inject
    EntityManager em;

    /**
     * Lấy thống kê doanh thu theo khoảng thời gian.
     * Chỉ tính đơn hàng DELIVERED.
     */
    public RevenueResponse getRevenueSummary(LocalDate from, LocalDate to) {
        // Tổng doanh thu & số đơn
        @SuppressWarnings("unchecked")
        List<Object[]> totalResult = em.createNativeQuery(
                "SELECT COALESCE(SUM(total_amount), 0), COUNT(*) FROM orders " +
                "WHERE status = 'DELIVERED' AND created_at >= ?1 AND created_at < ?2")
                .setParameter(1, from.atStartOfDay())
                .setParameter(2, to.plusDays(1).atStartOfDay())
                .getResultList();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalOrders = 0;
        if (!totalResult.isEmpty()) {
            Object[] row = totalResult.get(0);
            totalRevenue = row[0] instanceof BigDecimal ? (BigDecimal) row[0] : new BigDecimal(row[0].toString());
            totalOrders = ((Number) row[1]).longValue();
        }

        // Doanh thu theo ngày
        @SuppressWarnings("unchecked")
        List<Object[]> dailyResult = em.createNativeQuery(
                "SELECT DATE(created_at) AS d, COALESCE(SUM(total_amount), 0), COUNT(*) FROM orders " +
                "WHERE status = 'DELIVERED' AND created_at >= ?1 AND created_at < ?2 " +
                "GROUP BY d ORDER BY d DESC")
                .setParameter(1, from.atStartOfDay())
                .setParameter(2, to.plusDays(1).atStartOfDay())
                .getResultList();

        List<RevenueResponse.DailyRevenue> dailyList = new ArrayList<>();
        for (Object[] row : dailyResult) {
            String date = row[0] != null ? row[0].toString() : "";
            BigDecimal revenue = row[1] instanceof BigDecimal ? (BigDecimal) row[1] : new BigDecimal(row[1].toString());
            long count = ((Number) row[2]).longValue();
            dailyList.add(new RevenueResponse.DailyRevenue(date, revenue, count));
        }

        // Trung bình doanh thu/ngày
        long dayCount = java.time.temporal.ChronoUnit.DAYS.between(from, to) + 1;
        BigDecimal avgPerDay = dayCount > 0
                ? totalRevenue.divide(BigDecimal.valueOf(dayCount), 0, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        LOG.infof("Revenue summary: %s → %s | Total: %s | Orders: %d", from, to, totalRevenue, totalOrders);
        return new RevenueResponse(totalRevenue, totalOrders, avgPerDay, dailyList);
    }
}
