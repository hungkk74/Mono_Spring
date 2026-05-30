package com.monowear.resource;

import com.monowear.dto.common.ApiResponse;
import com.monowear.dto.common.PagedResponse;
import com.monowear.dto.order.OrderResponse;
import com.monowear.dto.order.UpdateOrderStatusRequest;
import com.monowear.entity.enums.OrderStatus;
import com.monowear.service.OrderService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

/**
 * Admin/Staff Order Management — Xem tất cả đơn, cập nhật trạng thái.
 */
@Path("/api/admin/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "STAFF"})
public class AdminOrderResource {

    @Inject
    OrderService orderService;

    /**
     * GET /api/admin/orders — Danh sách tất cả đơn hàng (phân trang, lọc status).
     */
    @GET
    public RestResponse<ApiResponse<PagedResponse<OrderResponse>>> list(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("status") OrderStatus status,
            @QueryParam("search") String search) {
        return RestResponse.ok(ApiResponse.success(orderService.listAll(page, size, status, search)));
    }

    /**
     * GET /api/admin/orders/{id} — Chi tiết đơn hàng (bất kỳ).
     */
    @GET
    @Path("/{id}")
    public RestResponse<ApiResponse<OrderResponse>> getById(@PathParam("id") Long id) {
        return RestResponse.ok(ApiResponse.success(orderService.getById(id)));
    }

    /**
     * PATCH /api/admin/orders/{id}/status — Cập nhật trạng thái đơn hàng.
     */
    @PATCH
    @Path("/{id}/status")
    public RestResponse<ApiResponse<OrderResponse>> updateStatus(
            @PathParam("id") Long id, @Valid UpdateOrderStatusRequest request) {
        return RestResponse.ok(ApiResponse.success("Cập nhật trạng thái thành công",
                orderService.updateStatus(id, request)));
    }
}
