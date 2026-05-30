package com.monowear.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Chuẩn hóa Response cho toàn bộ API.
 *
 * @param status  HTTP status code (200, 400, 404, 500...)
 * @param message Thông báo ngắn gọn cho client
 * @param data    Payload trả về (nullable)
 * @param errorCode Mã lỗi nội bộ cho frontend xử lý (nullable)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        int status,
        String message,
        T data,
        String errorCode
) {

    // --- Factory Methods ---

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "Thành công", data, null);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data, null);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(201, "Tạo thành công", data, null);
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(201, message, data, null);
    }

    public static ApiResponse<Void> noContent() {
        return new ApiResponse<>(204, "Xóa thành công", null, null);
    }

    public static <T> ApiResponse<T> error(int status, String message, String errorCode) {
        return new ApiResponse<>(status, message, null, errorCode);
    }

    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(400, message, null, "BAD_REQUEST");
    }

    public static <T> ApiResponse<T> unauthorized(String message) {
        return new ApiResponse<>(401, message, null, "UNAUTHORIZED");
    }

    public static <T> ApiResponse<T> forbidden(String message) {
        return new ApiResponse<>(403, message, null, "FORBIDDEN");
    }

    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(404, message, null, "NOT_FOUND");
    }

    public static <T> ApiResponse<T> conflict(String message) {
        return new ApiResponse<>(409, message, null, "CONFLICT");
    }

    public static <T> ApiResponse<T> internalError() {
        return new ApiResponse<>(500, "Lỗi hệ thống, vui lòng thử lại sau", null, "INTERNAL_ERROR");
    }
}
