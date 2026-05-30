package com.monowear.dto.order;

import java.math.BigDecimal;

public record ReorderItemResponse(
        Long skuId,
        String skuCode,
        String productName,
        String productSlug,
        String productImageUrl,
        String size,
        String color,
        BigDecimal currentPrice,
        BigDecimal salePrice,
        Integer stock,
        boolean available,
        String unavailableReason
) {}
