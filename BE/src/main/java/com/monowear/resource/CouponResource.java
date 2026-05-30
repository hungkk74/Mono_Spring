package com.monowear.resource;

import com.monowear.dto.common.ApiResponse;
import com.monowear.dto.promotion.CouponApplyRequest;
import com.monowear.dto.promotion.CouponApplyResponse;
import com.monowear.service.CouponService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/api/public/coupons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@PermitAll
public class CouponResource {

    @Inject
    CouponService couponService;

    @POST
    @Path("/apply")
    public RestResponse<ApiResponse<CouponApplyResponse>> apply(@Valid CouponApplyRequest request) {
        return RestResponse.ok(ApiResponse.success("Áp dụng mã giảm giá thành công",
                couponService.validate(request.code(), request.subtotalAmount())));
    }
}
