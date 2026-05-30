package com.monowear.resource;

import com.monowear.dto.banner.BannerRequest;
import com.monowear.dto.banner.BannerResponse;
import com.monowear.dto.common.ApiResponse;
import com.monowear.dto.common.PagedResponse;
import com.monowear.service.BannerService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.List;

@Path("/api/admin/banners")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "STAFF"})
public class BannerResource {

    @Inject
    BannerService bannerService;

    @GET
    public RestResponse<ApiResponse<PagedResponse<BannerResponse>>> list(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        return RestResponse.ok(ApiResponse.success(bannerService.listAll(page, size)));
    }

    @GET
    @Path("/{id}")
    public RestResponse<ApiResponse<BannerResponse>> getById(@PathParam("id") Long id) {
        return RestResponse.ok(ApiResponse.success(bannerService.getById(id)));
    }

    @POST
    public RestResponse<ApiResponse<BannerResponse>> create(@Valid BannerRequest request) {
        BannerResponse response = bannerService.create(request);
        return RestResponse.status(RestResponse.Status.CREATED, ApiResponse.created(response));
    }

    @PUT
    @Path("/{id}")
    public RestResponse<ApiResponse<BannerResponse>> update(
            @PathParam("id") Long id, @Valid BannerRequest request) {
        return RestResponse.ok(ApiResponse.success("Cập nhật banner thành công", bannerService.update(id, request)));
    }

    @DELETE
    @Path("/{id}")
    public RestResponse<ApiResponse<Void>> delete(@PathParam("id") Long id) {
        bannerService.delete(id);
        return RestResponse.ok(ApiResponse.noContent());
    }

    @PUT
    @Path("/reorder")
    public RestResponse<ApiResponse<Void>> reorder(List<Long> ids) {
        bannerService.reorder(ids);
        return RestResponse.ok(ApiResponse.success("Sắp xếp banner thành công", null));
    }
}
