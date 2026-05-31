package com.monowear.dto.order;

import com.monowear.entity.Order;
import com.monowear.entity.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        String userFullName,
        OrderStatus status,
        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        String couponCode,
        BigDecimal totalAmount,
        String shippingAddress,
        String paymentMethod,
        List<OrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getUser().getFullName(),
                order.getStatus(),
                order.getSubtotalAmount(),
                order.getDiscountAmount(),
                order.getCouponCode(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                order.getPaymentMethod(),
                null,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    public static OrderResponse withItems(Order order) {
        List<OrderItemResponse> itemList = null;
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            itemList = order.getItems().stream()
                    .map(OrderItemResponse::from)
                    .toList();
        }
        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getUser().getFullName(),
                order.getStatus(),
                order.getSubtotalAmount(),
                order.getDiscountAmount(),
                order.getCouponCode(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                order.getPaymentMethod(),
                itemList,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
