package com.monowear.dto.order;

import com.monowear.entity.OrderItem;
import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long skuId,
        String skuCode,
        String productName,
        String size,
        String color,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getSku().getId(),
                item.getSku().getSkuCode(),
                item.getSku().getProduct().getName(),
                item.getSku().getSize(),
                item.getSku().getColor(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }
}
