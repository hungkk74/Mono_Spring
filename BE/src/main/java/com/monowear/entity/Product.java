package com.monowear.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "products")
public class Product extends io.quarkus.hibernate.orm.panache.PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    public Category category;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @org.hibernate.annotations.BatchSize(size = 100)
    public List<Sku> skus;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false, unique = true)
    public String slug;

    public String material;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column(name = "image_url", length = 500)
    public String imageUrl;

    @Column(name = "sale_percent")
    public Integer salePercent;

    @Column(name = "sale_start_at")
    public LocalDateTime saleStartAt;

    @Column(name = "sale_end_at")
    public LocalDateTime saleEndAt;

    @Column(name = "is_active", nullable = false)
    public Boolean isActive = true;

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

    public static Optional<Product> findBySlug(String slug) {
        return find("slug", slug).firstResultOptional();
    }

    public static List<Product> listActive() {
        return list("isActive", true);
    }

    public static List<Product> listByCategory(Long categoryId) {
        return list("category.id = ?1 AND isActive = true", categoryId);
    }

    public static List<Product> search(String keyword) {
        return list("isActive = true AND (LOWER(name) LIKE ?1 OR LOWER(description) LIKE ?1 OR LOWER(material) LIKE ?1 OR LOWER(category.name) LIKE ?1)",
                "%" + keyword.toLowerCase() + "%");
    }

    public boolean isOnSale() {
        if (salePercent == null || salePercent <= 0) return false;
        LocalDateTime now = LocalDateTime.now();
        if (saleStartAt != null && saleStartAt.isAfter(now)) return false;
        if (saleEndAt != null && saleEndAt.isBefore(now)) return false;
        return true;
    }
}
