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
    /**
     * Mapping flat (không kèm items).
     */
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.id,
                order.user.id,
                order.user.fullName,
                order.status,
                order.subtotalAmount,
                order.discountAmount,
                order.couponCode,
                order.totalAmount,
                order.shippingAddress,
                order.paymentMethod,
                null,
                order.createdAt,
                order.updatedAt
        );
    }

    /**
     * Mapping kèm danh sách items (chi tiết đơn hàng).
     */
    public static OrderResponse withItems(Order order) {
        List<OrderItemResponse> itemList = null;
        if (order.items != null && !order.items.isEmpty()) {
            itemList = order.items.stream()
                    .map(OrderItemResponse::from)
                    .toList();
        }
        return new OrderResponse(
                order.id,
                order.user.id,
                order.user.fullName,
                order.status,
                order.subtotalAmount,
                order.discountAmount,
                order.couponCode,
                order.totalAmount,
                order.shippingAddress,
                order.paymentMethod,
                itemList,
                order.createdAt,
                order.updatedAt
        );
    }
}
