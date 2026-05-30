package com.monowear.dto.catalog;

import com.monowear.entity.Product;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        String slug,
        String material,
        String description,
        String imageUrl,
        Integer salePercent,
        LocalDateTime saleStartAt,
        LocalDateTime saleEndAt,
        Boolean onSale,
        BigDecimal minPrice,
        BigDecimal salePrice,
        Boolean isActive,
        Long categoryId,
        String categoryName,
        List<SkuResponse> skus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * Mapping flat (không kèm SKU).
     */
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.id,
                product.name,
                product.slug,
                product.material,
                product.description,
                product.imageUrl,
                product.salePercent,
                product.saleStartAt,
                product.saleEndAt,
                product.isOnSale(),
                minActivePrice(product),
                salePrice(product, minActivePrice(product)),
                product.isActive,
                product.category.id,
                product.category.name,
                null,
                product.createdAt,
                product.updatedAt
        );
    }

    /**
     * Mapping kèm danh sách SKU (cho trang chi tiết).
     */
    public static ProductResponse withSkus(Product product) {
        List<SkuResponse> skuList = null;
        if (product.skus != null && !product.skus.isEmpty()) {
            skuList = product.skus.stream()
                    .filter(s -> s.isActive)
                    .map(SkuResponse::from)
                    .toList();
        }
        return new ProductResponse(
                product.id,
                product.name,
                product.slug,
                product.material,
                product.description,
                product.imageUrl,
                product.salePercent,
                product.saleStartAt,
                product.saleEndAt,
                product.isOnSale(),
                minActivePrice(product),
                salePrice(product, minActivePrice(product)),
                product.isActive,
                product.category.id,
                product.category.name,
                skuList,
                product.createdAt,
                product.updatedAt
        );
    }

    private static BigDecimal minActivePrice(Product product) {
        if (product.skus == null || product.skus.isEmpty()) return null;
        return product.skus.stream()
                .filter(sku -> Boolean.TRUE.equals(sku.isActive) && sku.price != null)
                .min(Comparator.comparing(sku -> sku.price))
                .map(sku -> sku.price)
                .orElse(null);
    }

    private static BigDecimal salePrice(Product product, BigDecimal price) {
        if (price == null || !product.isOnSale()) return null;
        return price
                .multiply(BigDecimal.valueOf(100L - product.salePercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
