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
                item.id,
                item.sku.id,
                item.sku.skuCode,
                item.sku.product.name,
                item.sku.size,
                item.sku.color,
                item.quantity,
                item.unitPrice,
                item.subtotal
        );
    }
}
