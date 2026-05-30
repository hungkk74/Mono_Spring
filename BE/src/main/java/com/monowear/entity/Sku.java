package com.monowear.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "skus")
public class Sku extends io.quarkus.hibernate.orm.panache.PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    public Product product;

    @Column(name = "sku_code", nullable = false, unique = true, length = 100)
    public String skuCode;

    @Column(nullable = false, length = 20)
    public String size;

    @Column(nullable = false, length = 50)
    public String color;

    @Column(nullable = false, precision = 15, scale = 2)
    public BigDecimal price;

    @Column(nullable = false)
    public Integer stock = 0;

    @Column(name = "is_active", nullable = false)
    public Boolean isActive = true;

    @Version
    @Column(nullable = false)
    public Integer version = 0;

    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    // --- Lifecycle Callbacks ---

    @PrePersist
    void onPrePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onPreUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- Query Methods ---

    public static Optional<Sku> findBySkuCode(String skuCode) {
        return find("skuCode", skuCode).firstResultOptional();
    }

    public static List<Sku> listByProduct(Long productId) {
        return list("product.id = ?1 AND isActive = true", productId);
    }

    public static List<Sku> listInStock(Long productId) {
        return list("product.id = ?1 AND isActive = true AND stock > 0", productId);
    }

    /**
     * Atomic stock deduction using optimistic locking.
     * Returns the number of updated rows (1 = success, 0 = conflict/insufficient stock).
     */
    public static int deductStock(Long skuId, int quantity, int expectedVersion) {
        return update("stock = stock - ?1, version = version + 1 WHERE id = ?2 AND version = ?3 AND stock >= ?1",
                quantity, skuId, expectedVersion);
    }

    /**
     * Atomic stock restoration.
     * Returns the number of updated rows.
     */
    public static int restoreStock(Long skuId, int quantity) {
        return update("stock = stock + ?1, version = version + 1 WHERE id = ?2",
                quantity, skuId);
    }
}
