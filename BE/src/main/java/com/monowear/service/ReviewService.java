package com.monowear.service;

import com.monowear.dto.common.PagedResponse;
import com.monowear.dto.review.*;
import com.monowear.entity.*;
import com.monowear.entity.enums.OrderStatus;
import com.monowear.exception.BadRequestException;
import com.monowear.exception.ResourceNotFoundException;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class ReviewService {

    private static final Logger LOG = Logger.getLogger(ReviewService.class);

    /**
     * Lấy danh sách đánh giá của sản phẩm.
     */
    public List<ReviewResponse> getByProduct(Long productId) {
        List<Review> reviews = Review.find(
                "SELECT DISTINCT r FROM Review r " +
                "LEFT JOIN FETCH r.replies rp " +
                "LEFT JOIN FETCH rp.user " +
                "JOIN FETCH r.user " +
                "WHERE r.product.id = ?1 ORDER BY r.createdAt DESC", productId).list();
        return reviews.stream().map(ReviewResponse::from).toList();
    }

    /**
     * Lấy thống kê đánh giá (trung bình sao, tổng số review).
     */
    public ReviewStats getStats(Long productId) {
        // Single aggregate query — tránh load toàn bộ Review entities chỉ để đếm
        Object[] result = (Object[]) Review.getEntityManager()
                .createQuery("SELECT COUNT(r), AVG(r.rating) FROM Review r WHERE r.product.id = ?1")
                .setParameter(1, productId)
                .getSingleResult();
        long total = result[0] != null ? ((Number) result[0]).longValue() : 0L;
        double avg  = result[1] != null ? ((Number) result[1]).doubleValue() : 0.0;
        return new ReviewStats((int) total, Math.round(avg * 10.0) / 10.0);
    }

    /**
     * Tạo đánh giá mới — chỉ cho phép nếu user đã mua sản phẩm (đơn DELIVERED).
     */
    @Transactional
    public ReviewResponse create(Long userId, ReviewRequest request) {
        // Validate product
        Product product = Product.findById(request.productId());
        if (product == null || !product.isActive) {
            throw new ResourceNotFoundException("Sản phẩm", request.productId());
        }

        // Check duplicate
        Review existing = Review.findByProductAndUser(request.productId(), userId);
        if (existing != null) {
            throw new BadRequestException("Bạn đã đánh giá sản phẩm này rồi");
        }

        // Check if user has bought this product (DELIVERED order)
        long deliveredCount = Order.count(
                "SELECT count(o) FROM Order o JOIN o.items i JOIN i.sku s " +
                "WHERE o.user.id = ?1 AND s.product.id = ?2 AND o.status = ?3",
                userId, request.productId(), OrderStatus.DELIVERED
        );
        if (deliveredCount == 0) {
            throw new BadRequestException("Bạn cần mua sản phẩm này trước khi đánh giá");
        }

        // Create review
        User user = User.findById(userId);
        Review review = new Review();
        review.product = product;
        review.user = user;
        review.rating = request.rating();
        review.comment = request.comment();
        review.persist();

        LOG.infof("Review created: Product %d by User %d, Rating: %d", request.productId(), userId, request.rating());
        return ReviewResponse.from(review);
    }

    /**
     * Xóa đánh giá (chỉ owner hoặc admin).
     */
    @Transactional
    public void delete(Long reviewId, Long userId, boolean isAdmin) {
        Review review = Review.findById(reviewId);
        if (review == null) {
            throw new ResourceNotFoundException("Đánh giá", reviewId);
        }
        if (!isAdmin && !review.user.id.equals(userId)) {
            throw new BadRequestException("Bạn không có quyền xóa đánh giá này");
        }
        review.delete();
        LOG.infof("Review deleted: ID %d", reviewId);
    }

    // ==================== REVIEW REPLIES (ADMIN/STAFF) ====================

    /**
     * Admin/Staff phản hồi đánh giá.
     */
    @Transactional
    public ReviewReplyResponse replyToReview(Long userId, ReviewReplyRequest request) {
        Review review = Review.findById(request.reviewId());
        if (review == null) {
            throw new ResourceNotFoundException("Đánh giá", request.reviewId());
        }

        User user = User.findById(userId);
        if (user == null) {
            throw new BadRequestException("Người dùng không hợp lệ");
        }

        ReviewReply reply = new ReviewReply();
        reply.review = review;
        reply.user = user;
        reply.content = request.content().trim();
        reply.persist();

        LOG.infof("Review reply created: Review %d by User %d (role: %s)", request.reviewId(), userId, user.role);
        return ReviewReplyResponse.from(reply);
    }

    /**
     * Xóa phản hồi (chỉ admin hoặc chính người reply).
     */
    @Transactional
    public void deleteReply(Long replyId, Long userId, boolean isAdmin) {
        ReviewReply reply = ReviewReply.findById(replyId);
        if (reply == null) {
            throw new ResourceNotFoundException("Phản hồi", replyId);
        }
        if (!isAdmin && !reply.user.id.equals(userId)) {
            throw new BadRequestException("Bạn không có quyền xóa phản hồi này");
        }
        reply.delete();
        LOG.infof("Review reply deleted: ID %d", replyId);
    }

    /**
     * Lấy tất cả reviews (phân trang) — dành cho Admin Dashboard.
     */
    public PagedResponse<ReviewResponse> listAll(int page, int size) {
        long total = Review.count();

        // Step 1: Lấy IDs phân trang
        List<Long> ids = Review.find("SELECT r.id FROM Review r ORDER BY r.createdAt DESC")
                .page(Page.of(page, size))
                .project(Long.class)
                .list();

        if (ids.isEmpty()) {
            return PagedResponse.of(List.of(), page, size, total);
        }

        // Step 2: Fetch full data với JOIN FETCH (không pagination)
        List<Review> reviews = Review.find(
                "SELECT DISTINCT r FROM Review r " +
                "LEFT JOIN FETCH r.replies rp " +
                "LEFT JOIN FETCH rp.user " +
                "JOIN FETCH r.user " +
                "JOIN FETCH r.product " +
                "WHERE r.id IN ?1 ORDER BY r.createdAt DESC", ids).list();

        List<ReviewResponse> items = reviews.stream()
                .map(ReviewResponse::from)
                .toList();
        return PagedResponse.of(items, page, size, total);
    }

    // --- Inner record ---
    public record ReviewStats(int totalReviews, double averageRating) {}
}
