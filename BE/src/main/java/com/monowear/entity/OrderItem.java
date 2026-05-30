package com.monowear.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
public class OrderItem extends io.quarkus.hibernate.orm.panache.PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    public Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = false)
    public Sku sku;

    @Column(nullable = false)
    public Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    public BigDecimal unitPrice;

    @Column(nullable = false, precision = 15, scale = 2)
    public BigDecimal subtotal;

    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    // --- Lifecycle Callbacks ---

    @PrePersist
    void onPrePersist() {
        createdAt = LocalDateTime.now();
        subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
