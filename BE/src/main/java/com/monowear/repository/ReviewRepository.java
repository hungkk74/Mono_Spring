package com.monowear.repository;

import com.monowear.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);

    long countByProductId(Long productId);

    Optional<Review> findByProductIdAndUserId(Long productId, Long userId);

    @Query("SELECT DISTINCT r FROM Review r LEFT JOIN FETCH r.replies rp LEFT JOIN FETCH rp.user " +
           "JOIN FETCH r.user WHERE r.product.id = :productId ORDER BY r.createdAt DESC")
    List<Review> findByProductWithReplies(@Param("productId") Long productId);

    @Query("SELECT DISTINCT r FROM Review r LEFT JOIN FETCH r.replies rp LEFT JOIN FETCH rp.user " +
           "JOIN FETCH r.user JOIN FETCH r.product WHERE r.id IN :ids ORDER BY r.createdAt DESC")
    List<Review> findAllWithDetailsByIds(@Param("ids") List<Long> ids);
}
