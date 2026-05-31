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
        int itemCount = order.getItems() == null
                ? 0
                : order.getItems().stream()
                        .mapToInt(item -> item.getQuantity() == null ? 0 : item.getQuantity())
                        .sum();

        return new OrderTrackingResponse(
                order.getId(),
                order.getStatus(),
                order.getSubtotalAmount(),
                order.getDiscountAmount(),
                order.getCouponCode(),
                order.getTotalAmount(),
                order.getPaymentMethod(),
                itemCount,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
