package com.monowear.resource;

import com.monowear.dto.catalog.ProductRequest;
import com.monowear.dto.catalog.ProductResponse;
import com.monowear.dto.common.ApiResponse;
import com.monowear.dto.common.PagedResponse;
import com.monowear.service.ProductService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/api/admin/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "STAFF"})
public class ProductResource {

    @Inject
    ProductService productService;

    @GET
    public RestResponse<ApiResponse<PagedResponse<ProductResponse>>> list(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("categoryId") Long categoryId,
            @QueryParam("keyword") String keyword) {
        return RestResponse.ok(ApiResponse.success(productService.listAll(page, size, categoryId, keyword)));
    }

    @GET
    @Path("/{id}")
    public RestResponse<ApiResponse<ProductResponse>> getById(@PathParam("id") Long id) {
        return RestResponse.ok(ApiResponse.success(productService.getById(id)));
    }

    @POST
    public RestResponse<ApiResponse<ProductResponse>> create(@Valid ProductRequest request) {
        ProductResponse response = productService.create(request);
        return RestResponse.status(RestResponse.Status.CREATED, ApiResponse.created(response));
    }

    @PUT
    @Path("/{id}")
    public RestResponse<ApiResponse<ProductResponse>> update(
            @PathParam("id") Long id, @Valid ProductRequest request) {
        return RestResponse.ok(ApiResponse.success("Cập nhật thành công", productService.update(id, request)));
    }

    @DELETE
    @Path("/{id}")
    public RestResponse<ApiResponse<Void>> delete(@PathParam("id") Long id) {
        productService.delete(id);
        return RestResponse.ok(ApiResponse.noContent());
    }
}
