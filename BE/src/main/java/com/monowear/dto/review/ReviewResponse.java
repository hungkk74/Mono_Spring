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
        List<ReviewReplyResponse> replyList = (review.getReplies() != null)
                ? review.getReplies().stream().map(ReviewReplyResponse::from).toList()
                : List.of();
        return new ReviewResponse(
                review.getId(),
                review.getProduct().getId(),
                review.getUser().getId(),
                review.getUser().getFullName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                replyList
        );
    }
}
