package com.monowear.resource;

import com.monowear.dto.common.ApiResponse;
import com.monowear.dto.common.PagedResponse;
import com.monowear.dto.order.OrderRequest;
import com.monowear.dto.order.OrderResponse;
import com.monowear.dto.order.ReorderItemResponse;
import com.monowear.service.OrderService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestResponse;

/**
 * Customer Order APIs — Đặt hàng, xem đơn, hủy đơn.
 */
@Path("/api/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"CUSTOMER", "ADMIN", "STAFF"})
public class OrderResource {

    @Inject
    OrderService orderService;

    @Inject
    JsonWebToken jwt;

    /**
     * POST /api/orders — Đặt hàng (Checkout).
     */
    @POST
    public RestResponse<ApiResponse<OrderResponse>> placeOrder(@Valid OrderRequest request) {
        Long userId = Long.parseLong(jwt.getSubject());
        OrderResponse response = orderService.placeOrder(userId, request);
        return RestResponse.status(
                RestResponse.Status.CREATED,
                ApiResponse.created("Đặt hàng thành công", response)
        );
    }

    /**
     * GET /api/orders — Xem danh sách đơn hàng của tôi.
     */
    @GET
    public RestResponse<ApiResponse<PagedResponse<OrderResponse>>> myOrders(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        Long userId = Long.parseLong(jwt.getSubject());
        return RestResponse.ok(ApiResponse.success(orderService.listByUser(userId, page, size)));
    }

    /**
     * GET /api/orders/{id} — Xem chi tiết đơn hàng của tôi.
     */
    @GET
    @Path("/{id}")
    public RestResponse<ApiResponse<OrderResponse>> myOrderDetail(@PathParam("id") Long id) {
        Long userId = Long.parseLong(jwt.getSubject());
        return RestResponse.ok(ApiResponse.success(orderService.getByIdForUser(id, userId)));
    }

    /**
     * PATCH /api/orders/{id}/cancel — Hủy đơn hàng (chỉ PENDING).
     */
    @PATCH
    @Path("/{id}/cancel")
    public RestResponse<ApiResponse<OrderResponse>> cancelOrder(@PathParam("id") Long id) {
        Long userId = Long.parseLong(jwt.getSubject());
        return RestResponse.ok(ApiResponse.success("Hủy đơn hàng thành công", orderService.cancelOrder(id, userId)));
    }

    /**
     * POST /api/orders/{id}/reorder — Mua lại: trả về danh sách items với giá/tồn kho hiện tại.
     */
    @POST
    @Path("/{id}/reorder")
    public RestResponse<ApiResponse<java.util.List<ReorderItemResponse>>> reorder(@PathParam("id") Long id) {
        Long userId = Long.parseLong(jwt.getSubject());
        return RestResponse.ok(ApiResponse.success(orderService.getReorderItems(id, userId)));
    }
}
