package com.monowear.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Entity
@Table(name = "coupons")
public class Coupon extends io.quarkus.hibernate.orm.panache.PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, unique = true, length = 50)
    public String code;

    @Column(length = 255)
    public String description;

    @Column(name = "discount_type", nullable = false, length = 20)
    public String discountType;

    @Column(name = "discount_value", nullable = false, precision = 15, scale = 2)
    public BigDecimal discountValue;

    @Column(name = "max_discount_amount", precision = 15, scale = 2)
    public BigDecimal maxDiscountAmount;

    @Column(name = "min_order_amount", nullable = false, precision = 15, scale = 2)
    public BigDecimal minOrderAmount = BigDecimal.ZERO;

    @Column(name = "usage_limit")
    public Integer usageLimit;

    @Column(name = "used_count", nullable = false)
    public Integer usedCount = 0;

    @Column(name = "is_active", nullable = false)
    public Boolean isActive = true;

    @Column(name = "start_at")
    public LocalDateTime startAt;

    @Column(name = "end_at")
    public LocalDateTime endAt;

    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @PrePersist
    void onPrePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onPreUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static Optional<Coupon> findByCode(String code) {
        return find("UPPER(code)", code.trim().toUpperCase()).firstResultOptional();
    }
}
