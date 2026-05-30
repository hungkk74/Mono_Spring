package com.monowear.resource;

import com.monowear.dto.auth.CreateStaffRequest;
import com.monowear.dto.auth.UserResponse;
import com.monowear.dto.common.ApiResponse;
import com.monowear.dto.common.PagedResponse;
import com.monowear.entity.User;
import com.monowear.entity.enums.UserRole;
import com.monowear.service.UserService;
import io.quarkus.panache.common.Page;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.List;

/**
 * Admin User Management — CRUD nhân viên (chỉ ADMIN).
 */
@Path("/api/admin/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
public class AdminUserResource {

    @Inject
    UserService userService;

    /**
     * GET /api/admin/users?role=STAFF&page=0&size=20 — Danh sách user theo role.
     */
    @GET
    public RestResponse<ApiResponse<PagedResponse<UserResponse>>> list(
            @QueryParam("role") UserRole role,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {

        String query = "1=1";
        var params = new java.util.HashMap<String, Object>();
        if (role != null) {
            query += " AND role = :role";
            params.put("role", role);
        }
        query += " ORDER BY createdAt DESC";

        long total = User.count(query.replace(" ORDER BY createdAt DESC", ""), params);
        List<UserResponse> items = User.find(query, params)
                .page(Page.of(page, size))
                .list()
                .stream()
                .map(u -> UserResponse.from((User) u))
                .toList();

        return RestResponse.ok(ApiResponse.success(PagedResponse.of(items, page, size, total)));
    }

    /**
     * POST /api/admin/users/staff — Tạo tài khoản nhân viên.
     */
    @POST
    @Path("/staff")
    public RestResponse<ApiResponse<UserResponse>> createStaff(@Valid CreateStaffRequest request) {
        UserResponse response = userService.createStaff(request);
        return RestResponse.status(
                RestResponse.Status.CREATED,
                ApiResponse.created("Tạo nhân viên thành công", response)
        );
    }

    /**
     * DELETE /api/admin/users/{id} — Vô hiệu hóa tài khoản (soft delete).
     */
    @DELETE
    @Path("/{id}")
    public RestResponse<ApiResponse<Void>> deactivate(@PathParam("id") Long id) {
        userService.deactivateUser(id);
        return RestResponse.ok(ApiResponse.noContent());
    }

    /**
     * PATCH /api/admin/users/{id}/activate — Kích hoạt lại tài khoản.
     */
    @PATCH
    @Path("/{id}/activate")
    public RestResponse<ApiResponse<UserResponse>> activate(@PathParam("id") Long id) {
        UserResponse response = userService.activateUser(id);
        return RestResponse.ok(ApiResponse.success("Kích hoạt thành công", response));
    }
}
