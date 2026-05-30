package com.monowear.entity;

import com.monowear.entity.enums.BannerMediaType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "banners")
public class Banner extends io.quarkus.hibernate.orm.panache.PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String title;

    @Column(length = 500)
    public String subtitle;

    @Column(name = "media_url", nullable = false, length = 500)
    public String mediaUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 10)
    public BannerMediaType mediaType = BannerMediaType.IMAGE;

    @Column(name = "thumbnail_url", length = 500)
    public String thumbnailUrl;

    @Column(name = "link_url", length = 500)
    public String linkUrl;

    @Column(name = "cta_text", length = 100)
    public String ctaText;

    @Column(name = "display_order", nullable = false)
    public Integer displayOrder = 0;

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

    public static List<Banner> listActiveOrdered() {
        return list("isActive = true ORDER BY displayOrder ASC");
    }

    public static long countActive() {
        return count("isActive", true);
    }

    public static int maxDisplayOrder() {
        Integer max = find("SELECT MAX(b.displayOrder) FROM Banner b")
                .project(Integer.class)
                .firstResult();
        return max != null ? max : 0;
    }
}
