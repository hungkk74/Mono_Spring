package com.monowear.dto.catalog;

import com.monowear.entity.Sku;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SkuResponse(
        Long id,
        String skuCode,
        String size,
        String color,
        BigDecimal price,
        Integer stock,
        Boolean isActive,
        Long productId,
        String productName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static SkuResponse from(Sku sku) {
        return new SkuResponse(
                sku.getId(),
                sku.getSkuCode(),
                sku.getSize(),
                sku.getColor(),
                sku.getPrice(),
                sku.getStock(),
                sku.getIsActive(),
                sku.getProduct().getId(),
                sku.getProduct().getName(),
                sku.getCreatedAt(),
                sku.getUpdatedAt()
        );
    }
}
