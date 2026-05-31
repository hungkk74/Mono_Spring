package com.monowear.service;

import com.monowear.dto.common.PagedResponse;
import com.monowear.dto.review.*;
import com.monowear.entity.*;
import com.monowear.entity.enums.OrderStatus;
import com.monowear.exception.BadRequestException;
import com.monowear.exception.ResourceNotFoundException;
import com.monowear.repository.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final EntityManager em;

    public List<ReviewResponse> getByProduct(Long productId) {
        return reviewRepository.findByProductWithReplies(productId)
                .stream().map(ReviewResponse::from).toList();
    }

    public ReviewStats getStats(Long productId) {
        Object[] result = (Object[]) em
                .createQuery("SELECT COUNT(r), AVG(r.rating) FROM Review r WHERE r.product.id = ?1")
                .setParameter(1, productId)
                .getSingleResult();
        long total = result[0] != null ? ((Number) result[0]).longValue() : 0L;
        double avg = result[1] != null ? ((Number) result[1]).doubleValue() : 0.0;
        return new ReviewStats((int) total, Math.round(avg * 10.0) / 10.0);
    }

    @Transactional
    public ReviewResponse create(Long userId, ReviewRequest request) {
        Product product = productRepository.findById(request.productId()).orElse(null);
        if (product == null || !product.getIsActive()) {
            throw new ResourceNotFoundException("Sản phẩm", request.productId());
        }

        reviewRepository.findByProductIdAndUserId(request.productId(), userId).ifPresent(r -> {
            throw new BadRequestException("Bạn đã đánh giá sản phẩm này rồi");
        });

        long deliveredCount = em.createQuery(
                "SELECT count(o) FROM Order o JOIN o.items i JOIN i.sku s " +
                "WHERE o.user.id = ?1 AND s.product.id = ?2 AND o.status = ?3", Long.class)
                .setParameter(1, userId)
                .setParameter(2, request.productId())
                .setParameter(3, OrderStatus.DELIVERED)
                .getSingleResult();
        if (deliveredCount == 0) {
            throw new BadRequestException("Bạn cần mua sản phẩm này trước khi đánh giá");
        }

        User user = userRepository.findById(userId).orElseThrow();
        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(request.rating());
        review.setComment(request.comment());
        reviewRepository.save(review);

        log.info("Review created: Product {} by User {}, Rating: {}", request.productId(), userId, request.rating());
        return ReviewResponse.from(review);
    }

    @Transactional
    public void delete(Long reviewId, Long userId, boolean isAdmin) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá", reviewId));
        if (!isAdmin && !review.getUser().getId().equals(userId)) {
            throw new BadRequestException("Bạn không có quyền xóa đánh giá này");
        }
        reviewRepository.delete(review);
        log.info("Review deleted: ID {}", reviewId);
    }

    @Transactional
    public ReviewReplyResponse replyToReview(Long userId, ReviewReplyRequest request) {
        Review review = reviewRepository.findById(request.reviewId())
                .orElseThrow(() -> new ResourceNotFoundException("Đánh giá", request.reviewId()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Người dùng không hợp lệ"));

        ReviewReply reply = new ReviewReply();
        reply.setReview(review);
        reply.setUser(user);
        reply.setContent(request.content().trim());
        reviewReplyRepository.save(reply);

        log.info("Review reply created: Review {} by User {} (role: {})", request.reviewId(), userId, user.getRole());
        return ReviewReplyResponse.from(reply);
    }

    @Transactional
    public void deleteReply(Long replyId, Long userId, boolean isAdmin) {
        ReviewReply reply = reviewReplyRepository.findById(replyId)
                .orElseThrow(() -> new ResourceNotFoundException("Phản hồi", replyId));
        if (!isAdmin && !reply.getUser().getId().equals(userId)) {
            throw new BadRequestException("Bạn không có quyền xóa phản hồi này");
        }
        reviewReplyRepository.delete(reply);
        log.info("Review reply deleted: ID {}", replyId);
    }

    public PagedResponse<ReviewResponse> listAll(int page, int size) {
        long total = reviewRepository.count();

        List<Long> ids = em.createQuery("SELECT r.id FROM Review r ORDER BY r.createdAt DESC", Long.class)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();

        if (ids.isEmpty()) return PagedResponse.of(List.of(), page, size, total);

        List<ReviewResponse> items = reviewRepository.findAllWithDetailsByIds(ids)
                .stream().map(ReviewResponse::from).toList();
        return PagedResponse.of(items, page, size, total);
    }

    public record ReviewStats(int totalReviews, double averageRating) {}
}
