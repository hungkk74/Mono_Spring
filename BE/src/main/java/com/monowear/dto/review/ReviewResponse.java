package com.monowear.dto.review;

import com.monowear.entity.Review;
import java.time.LocalDateTime;
import java.util.List;

public record ReviewResponse(
        Long id,
        Long productId,
        Long userId,
        String userFullName,
        Integer rating,
        String comment,
        LocalDateTime createdAt,
        List<ReviewReplyResponse> replies
) {
    public static ReviewResponse from(Review review) {
        List<ReviewReplyResponse> replyList = (review.replies != null)
                ? review.replies.stream().map(ReviewReplyResponse::from).toList()
                : List.of();
        return new ReviewResponse(
                review.id,
                review.product.id,
                review.user.id,
                review.user.fullName,
                review.rating,
                review.comment,
                review.createdAt,
                replyList
        );
    }
}
