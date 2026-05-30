package com.monowear.resource;

import com.monowear.dto.auth.*;
import com.monowear.dto.common.ApiResponse;
import com.monowear.service.AuthService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    @Inject
    JsonWebToken jwt;

    /**
     * POST /api/auth/register — Đăng ký tài khoản mới (Public).
     */
    @POST
    @Path("/register")
    @PermitAll
    public RestResponse<ApiResponse<AuthResponse>> register(@Valid RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return RestResponse.status(
                RestResponse.Status.CREATED,
                ApiResponse.created("Đăng ký thành công", response)
        );
    }

    /**
     * POST /api/auth/login — Đăng nhập (Public).
     */
    @POST
    @Path("/login")
    @PermitAll
    public RestResponse<ApiResponse<AuthResponse>> login(@Valid LoginRequest request) {
        AuthResponse response = authService.login(request);
        return RestResponse.ok(ApiResponse.success("Đăng nhập thành công", response));
    }

    /**
     * GET /api/auth/me — Lấy thông tin user hiện tại (Authenticated).
     */
    @GET
    @Path("/me")
    @RolesAllowed({"ADMIN", "STAFF", "CUSTOMER"})
    public RestResponse<ApiResponse<UserResponse>> me() {
        UserResponse response = authService.getCurrentUser(jwt.getSubject());
        return RestResponse.ok(ApiResponse.success(response));
    }

    /**
     * PUT /api/auth/me — Cập nhật hồ sơ cá nhân (Authenticated).
     */
    @PUT
    @Path("/me")
    @RolesAllowed({"ADMIN", "STAFF", "CUSTOMER"})
    public RestResponse<ApiResponse<UserResponse>> updateProfile(@Valid UpdateProfileRequest request) {
        UserResponse response = authService.updateProfile(jwt.getSubject(), request);
        return RestResponse.ok(ApiResponse.success("Cập nhật hồ sơ thành công", response));
    }

    // ===================== FORGOT PASSWORD FLOW =====================

    /**
     * POST /api/auth/forgot-password — Bước 1: Yêu cầu OTP (Public).
     * Luôn trả 200 dù email có tồn tại hay không (tránh email enumeration).
     */
    @POST
    @Path("/forgot-password")
    @PermitAll
    public RestResponse<ApiResponse<Void>> forgotPassword(@Valid ForgotPasswordRequest request) {
        authService.requestOtp(request.email());
        return RestResponse.ok(ApiResponse.success(
                "Nếu email tồn tại, mã OTP đã được gửi. Vui lòng kiểm tra hộp thư.", null));
    }

    /**
     * POST /api/auth/verify-otp — Bước 2: Xác thực OTP, nhận Reset Token (Public).
     */
    @POST
    @Path("/verify-otp")
    @PermitAll
    public RestResponse<ApiResponse<ResetTokenResponse>> verifyOtp(@Valid VerifyOtpRequest request) {
        ResetTokenResponse response = authService.verifyOtp(request.email(), request.code());
        return RestResponse.ok(ApiResponse.success("Xác thực OTP thành công", response));
    }

    /**
     * POST /api/auth/reset-password — Bước 3: Đặt mật khẩu mới (Public, token-gated).
     */
    @POST
    @Path("/reset-password")
    @PermitAll
    public RestResponse<ApiResponse<Void>> resetPassword(@Valid ResetPasswordRequest request) {
        authService.resetPassword(request.resetToken(), request.newPassword());
        return RestResponse.ok(ApiResponse.success("Đặt lại mật khẩu thành công. Vui lòng đăng nhập.", null));
    }
}

