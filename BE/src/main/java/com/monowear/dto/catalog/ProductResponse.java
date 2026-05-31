package com.monowear.dto.catalog;

import com.monowear.entity.Product;
import com.monowear.entity.Sku;
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
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getMaterial(),
                product.getDescription(),
                product.getImageUrl(),
                product.getSalePercent(),
                product.getSaleStartAt(),
                product.getSaleEndAt(),
                product.isOnSale(),
                minActivePrice(product),
                salePrice(product, minActivePrice(product)),
                product.getIsActive(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                null,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    /**
     * Mapping flat kèm danh sách SKU bên ngoài để tránh N+1.
     */
    public static ProductResponse from(Product product, List<Sku> activeSkus) {
        BigDecimal minPrice = activeSkus == null || activeSkus.isEmpty() ? null : activeSkus.stream()
                .filter(sku -> sku.getPrice() != null)
                .min(Comparator.comparing(Sku::getPrice))
                .map(Sku::getPrice)
                .orElse(null);

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getMaterial(),
                product.getDescription(),
                product.getImageUrl(),
                product.getSalePercent(),
                product.getSaleStartAt(),
                product.getSaleEndAt(),
                product.isOnSale(),
                minPrice,
                salePrice(product, minPrice),
                product.getIsActive(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                null,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }


    /**
     * Mapping kèm danh sách SKU (cho trang chi tiết).
     */
    public static ProductResponse withSkus(Product product) {
        List<SkuResponse> skuList = null;
        if (product.getSkus() != null && !product.getSkus().isEmpty()) {
            skuList = product.getSkus().stream()
                    .filter(s -> s.getIsActive())
                    .map(SkuResponse::from)
                    .toList();
        }
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getMaterial(),
                product.getDescription(),
                product.getImageUrl(),
                product.getSalePercent(),
                product.getSaleStartAt(),
                product.getSaleEndAt(),
                product.isOnSale(),
                minActivePrice(product),
                salePrice(product, minActivePrice(product)),
                product.getIsActive(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                skuList,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    private static BigDecimal minActivePrice(Product product) {
        if (product.getSkus() == null || product.getSkus().isEmpty()) return null;
        return product.getSkus().stream()
                .filter(sku -> Boolean.TRUE.equals(sku.getIsActive()) && sku.getPrice() != null)
                .min(Comparator.comparing(sku -> sku.getPrice()))
                .map(sku -> sku.getPrice())
                .orElse(null);
    }

    private static BigDecimal salePrice(Product product, BigDecimal price) {
        if (price == null || !product.isOnSale()) return null;
        return price
                .multiply(BigDecimal.valueOf(100L - product.getSalePercent()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
