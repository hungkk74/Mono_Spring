package com.monowear.resource;

import com.monowear.dto.common.ApiResponse;
import com.monowear.dto.common.PagedResponse;
import com.monowear.dto.review.*;
import com.monowear.service.ReviewService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.List;

/**
 * Review APIs — Public GET, Authenticated POST/DELETE, Admin Reply.
 */
@Path("/api/reviews")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReviewResource {

    @Inject
    ReviewService reviewService;

    @Inject
    JsonWebToken jwt;



    /**
     * GET /api/reviews/all?page=0&size=15 — Admin: tất cả đánh giá (phân trang).
     */
    @GET
    @Path("/all")
    @RolesAllowed({"ADMIN", "STAFF"})
    public RestResponse<ApiResponse<PagedResponse<ReviewResponse>>> getAllReviews(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("15") int size) {
        return RestResponse.ok(ApiResponse.success(reviewService.listAll(page, size)));
    }

    /**
     * POST /api/reviews — Tạo đánh giá (yêu cầu đăng nhập).
     */
    @POST
    @RolesAllowed({"CUSTOMER", "ADMIN", "STAFF"})
    public RestResponse<ApiResponse<ReviewResponse>> createReview(@Valid ReviewRequest request) {
        Long userId = Long.parseLong(jwt.getSubject());
        ReviewResponse response = reviewService.create(userId, request);
        return RestResponse.status(RestResponse.Status.CREATED, ApiResponse.created("Đánh giá thành công", response));
    }

    /**
     * DELETE /api/reviews/{id} — Xóa đánh giá (owner hoặc admin).
     */
    @DELETE
    @Path("/{id}")
    @RolesAllowed({"CUSTOMER", "ADMIN", "STAFF"})
    public RestResponse<ApiResponse<Void>> deleteReview(@PathParam("id") Long id) {
        Long userId = Long.parseLong(jwt.getSubject());
        boolean isAdmin = jwt.getGroups().contains("ADMIN");
        reviewService.delete(id, userId, isAdmin);
        return RestResponse.ok(ApiResponse.noContent());
    }

    // ==================== REPLY ENDPOINTS ====================

    /**
     * POST /api/reviews/{id}/replies — Admin/Staff phản hồi đánh giá.
     */
    @POST
    @Path("/{id}/replies")
    @RolesAllowed({"ADMIN", "STAFF"})
    public RestResponse<ApiResponse<ReviewReplyResponse>> replyToReview(
            @PathParam("id") Long reviewId,
            @Valid ReviewReplyRequest request) {
        Long userId = Long.parseLong(jwt.getSubject());
        // Override reviewId from path
        ReviewReplyRequest fixedRequest = new ReviewReplyRequest(reviewId, request.content());
        ReviewReplyResponse response = reviewService.replyToReview(userId, fixedRequest);
        return RestResponse.status(RestResponse.Status.CREATED, ApiResponse.created("Phản hồi thành công", response));
    }

    /**
     * DELETE /api/reviews/replies/{replyId} — Xóa phản hồi.
     */
    @DELETE
    @Path("/replies/{replyId}")
    @RolesAllowed({"ADMIN", "STAFF"})
    public RestResponse<ApiResponse<Void>> deleteReply(@PathParam("replyId") Long replyId) {
        Long userId = Long.parseLong(jwt.getSubject());
        boolean isAdmin = jwt.getGroups().contains("ADMIN");
        reviewService.deleteReply(replyId, userId, isAdmin);
        return RestResponse.ok(ApiResponse.noContent());
    }
}
