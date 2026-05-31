package com.monowear.dto.review;

import com.monowear.entity.ReviewReply;
import java.time.LocalDateTime;

public record ReviewReplyResponse(
        Long id,
        Long reviewId,
        Long userId,
        String userFullName,
        String userRole,
        String content,
        LocalDateTime createdAt
) {
    public static ReviewReplyResponse from(ReviewReply reply) {
        return new ReviewReplyResponse(
                reply.getId(),
                reply.getReview().getId(),
                reply.getUser().getId(),
                reply.getUser().getFullName(),
                reply.getUser().getRole().name(),
                reply.getContent(),
                reply.getCreatedAt()
        );
    }
}
