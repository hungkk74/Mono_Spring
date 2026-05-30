package com.monowear.resource;

import com.monowear.dto.common.ApiResponse;
import com.monowear.dto.order.RevenueResponse;
import com.monowear.service.RevenueService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Admin Revenue Dashboard — Thống kê doanh thu bán hàng.
 */
@Path("/api/admin/revenue")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "STAFF"})
public class AdminRevenueResource {

    @Inject
    RevenueService revenueService;

    /**
     * GET /api/admin/revenue?period=7d|30d|90d&from=yyyy-MM-dd&to=yyyy-MM-dd
     */
    @GET
    public RestResponse<ApiResponse<RevenueResponse>> getRevenue(
            @QueryParam("period") @DefaultValue("30d") String period,
            @QueryParam("from") String fromStr,
            @QueryParam("to") String toStr) {

        LocalDate to = LocalDate.now();
        LocalDate from;

        if (fromStr != null && toStr != null) {
            try {
                from = LocalDate.parse(fromStr);
                to = LocalDate.parse(toStr);
            } catch (DateTimeParseException e) {
                return RestResponse.ok(ApiResponse.badRequest("Sai định dạng ngày (yyyy-MM-dd)"));
            }
        } else {
            from = switch (period) {
                case "7d" -> to.minusDays(6);
                case "90d" -> to.minusDays(89);
                default -> to.minusDays(29); // 30d
            };
        }

        return RestResponse.ok(ApiResponse.success(revenueService.getRevenueSummary(from, to)));
    }
}
