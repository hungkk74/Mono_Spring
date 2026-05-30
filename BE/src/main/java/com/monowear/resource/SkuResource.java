package com.monowear.resource;

import com.monowear.dto.catalog.SkuRequest;
import com.monowear.dto.catalog.SkuResponse;
import com.monowear.dto.common.ApiResponse;
import com.monowear.dto.common.PagedResponse;
import com.monowear.service.SkuService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/api/admin/skus")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "STAFF"})
public class SkuResource {

    @Inject
    SkuService skuService;

    @GET
    public RestResponse<ApiResponse<PagedResponse<SkuResponse>>> listByProduct(
            @QueryParam("productId") Long productId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        return RestResponse.ok(ApiResponse.success(skuService.listByProduct(productId, page, size)));
    }

    @GET
    @Path("/{id}")
    public RestResponse<ApiResponse<SkuResponse>> getById(@PathParam("id") Long id) {
        return RestResponse.ok(ApiResponse.success(skuService.getById(id)));
    }

    @POST
    public RestResponse<ApiResponse<SkuResponse>> create(@Valid SkuRequest request) {
        SkuResponse response = skuService.create(request);
        return RestResponse.status(RestResponse.Status.CREATED, ApiResponse.created(response));
    }

    @PUT
    @Path("/{id}")
    public RestResponse<ApiResponse<SkuResponse>> update(
            @PathParam("id") Long id, @Valid SkuRequest request) {
        return RestResponse.ok(ApiResponse.success("Cập nhật thành công", skuService.update(id, request)));
    }

    @DELETE
    @Path("/{id}")
    public RestResponse<ApiResponse<Void>> delete(@PathParam("id") Long id) {
        skuService.delete(id);
        return RestResponse.ok(ApiResponse.noContent());
    }
}
