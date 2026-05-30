package com.monowear.entity;

import com.monowear.entity.enums.OrderStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order extends io.quarkus.hibernate.orm.panache.PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public OrderStatus status = OrderStatus.PENDING;

    @Column(name = "subtotal_amount", nullable = false, precision = 15, scale = 2)
    public BigDecimal subtotalAmount;

    @Column(name = "discount_amount", nullable = false, precision = 15, scale = 2)
    public BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "coupon_code", length = 50)
    public String couponCode;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    public BigDecimal totalAmount;

    @Column(name = "shipping_address", nullable = false, columnDefinition = "TEXT")
    public String shippingAddress;

    @Column(name = "payment_method", nullable = false, length = 50)
    public String paymentMethod;

    @Version
    public Integer version;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    public List<OrderItem> items;

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

    public static List<Order> listByUser(Long userId) {
        return list("user.id = ?1 ORDER BY createdAt DESC", userId);
    }

    public static List<Order> listByStatus(OrderStatus status) {
        return list("status", status);
    }

    public static long countByUserAndStatus(Long userId, OrderStatus status) {
        return count("user.id = ?1 AND status = ?2", userId, status);
    }
}
