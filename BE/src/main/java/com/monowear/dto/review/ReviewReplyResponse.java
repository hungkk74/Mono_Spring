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
                reply.id,
                reply.review.id,
                reply.user.id,
                reply.user.fullName,
                reply.user.role.name(),
                reply.content,
                reply.createdAt
        );
    }
}
