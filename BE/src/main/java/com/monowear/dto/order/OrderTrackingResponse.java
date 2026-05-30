package com.monowear.dto.order;

import com.monowear.entity.Order;
import com.monowear.entity.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderTrackingResponse(
        Long id,
        OrderStatus status,
        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        String couponCode,
        BigDecimal totalAmount,
        String paymentMethod,
        Integer itemCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static OrderTrackingResponse from(Order order) {
        int itemCount = order.items == null
                ? 0
                : order.items.stream()
                        .mapToInt(item -> item.quantity == null ? 0 : item.quantity)
                        .sum();

        return new OrderTrackingResponse(
                order.id,
                order.status,
                order.subtotalAmount,
                order.discountAmount,
                order.couponCode,
                order.totalAmount,
                order.paymentMethod,
                itemCount,
                order.createdAt,
                order.updatedAt
        );
    }
}
