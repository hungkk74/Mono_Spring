package com.monowear.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "review_replies")
public class ReviewReply extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    public Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    public String content;

    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @PrePersist
    void onPrePersist() {
        createdAt = LocalDateTime.now();
    }

    // --- Query Methods ---

    public static List<ReviewReply> listByReview(Long reviewId) {
        return list("review.id = ?1 ORDER BY createdAt ASC", reviewId);
    }
}
