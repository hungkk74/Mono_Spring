package com.monowear.resource;

import com.monowear.dto.catalog.CategoryRequest;
import com.monowear.dto.catalog.CategoryResponse;
import com.monowear.dto.common.ApiResponse;
import com.monowear.dto.common.PagedResponse;
import com.monowear.service.CategoryService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/api/admin/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "STAFF"})
public class CategoryResource {

    @Inject
    CategoryService categoryService;

    @GET
    public RestResponse<ApiResponse<PagedResponse<CategoryResponse>>> list(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        return RestResponse.ok(ApiResponse.success(categoryService.listAll(page, size)));
    }

    @GET
    @Path("/{id}")
    public RestResponse<ApiResponse<CategoryResponse>> getById(@PathParam("id") Long id) {
        return RestResponse.ok(ApiResponse.success(categoryService.getById(id)));
    }

    @POST
    public RestResponse<ApiResponse<CategoryResponse>> create(@Valid CategoryRequest request) {
        CategoryResponse response = categoryService.create(request);
        return RestResponse.status(RestResponse.Status.CREATED, ApiResponse.created(response));
    }

    @PUT
    @Path("/{id}")
    public RestResponse<ApiResponse<CategoryResponse>> update(
            @PathParam("id") Long id, @Valid CategoryRequest request) {
        return RestResponse.ok(ApiResponse.success("Cập nhật thành công", categoryService.update(id, request)));
    }

    @DELETE
    @Path("/{id}")
    public RestResponse<ApiResponse<Void>> delete(@PathParam("id") Long id) {
        categoryService.delete(id);
        return RestResponse.ok(ApiResponse.noContent());
    }
}
