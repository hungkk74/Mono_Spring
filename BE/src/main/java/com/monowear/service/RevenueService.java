package com.monowear.service;

import com.monowear.dto.order.RevenueResponse;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RevenueService {

    private final EntityManager em;

    public RevenueResponse getRevenueSummary(LocalDate from, LocalDate to) {
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

        long dayCount = java.time.temporal.ChronoUnit.DAYS.between(from, to) + 1;
        BigDecimal avgPerDay = dayCount > 0
                ? totalRevenue.divide(BigDecimal.valueOf(dayCount), 0, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        log.info("Revenue summary: {} → {} | Total: {} | Orders: {}", from, to, totalRevenue, totalOrders);
        return new RevenueResponse(totalRevenue, totalOrders, avgPerDay, dailyList);
    }
}
