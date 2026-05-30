package com.monowear.exception;

import com.monowear.dto.common.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.util.stream.Collectors;

/**
 * Global Exception Handler — Tập trung xử lý tất cả exception,
 * trả về format ApiResponse chuẩn, KHÔNG văng stacktrace ra client.
 */
public class GlobalExceptionHandler {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionHandler.class);

    // --- Custom Business Exceptions ---

    @ServerExceptionMapper
    public RestResponse<ApiResponse<Void>> handleBadRequest(BadRequestException ex) {
        LOG.warnf("Bad Request: %s", ex.getMessage());
        return RestResponse.status(
                Response.Status.BAD_REQUEST,
                ApiResponse.error(400, ex.getMessage(), ex.getErrorCode())
        );
    }

    @ServerExceptionMapper
    public RestResponse<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        LOG.warnf("Not Found: %s", ex.getMessage());
        return RestResponse.status(
                Response.Status.NOT_FOUND,
                ApiResponse.notFound(ex.getMessage())
        );
    }

    @ServerExceptionMapper
    public RestResponse<ApiResponse<Void>> handleDuplicate(DuplicateResourceException ex) {
        LOG.warnf("Conflict: %s", ex.getMessage());
        return RestResponse.status(
                Response.Status.CONFLICT,
                ApiResponse.conflict(ex.getMessage())
        );
    }

    @ServerExceptionMapper
    public RestResponse<ApiResponse<Void>> handleInsufficientStock(InsufficientStockException ex) {
        LOG.warnf("Insufficient Stock: %s", ex.getMessage());
        return RestResponse.status(
                Response.Status.CONFLICT,
                ApiResponse.error(409, ex.getMessage(), "INSUFFICIENT_STOCK")
        );
    }

    // --- Hibernate Validator (Bean Validation) ---

    @ServerExceptionMapper
    public RestResponse<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        String details = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));

        LOG.warnf("Validation Error: %s", details);
        return RestResponse.status(
                Response.Status.BAD_REQUEST,
                ApiResponse.error(400, details, "VALIDATION_ERROR")
        );
    }

    // --- Jackson / JSON Parse Error ---

    @ServerExceptionMapper
    public RestResponse<ApiResponse<Void>> handleJsonParse(com.fasterxml.jackson.core.JsonProcessingException ex) {
        LOG.warnf("JSON Parse Error: %s", ex.getOriginalMessage());
        return RestResponse.status(
                Response.Status.BAD_REQUEST,
                ApiResponse.error(400, "Dữ liệu JSON không hợp lệ", "JSON_PARSE_ERROR")
        );
    }

    // --- Optimistic Locking (Race Condition) ---

    @ServerExceptionMapper
    public RestResponse<ApiResponse<Void>> handleOptimisticLock(jakarta.persistence.OptimisticLockException ex) {
        LOG.warnf("Optimistic Lock Conflict: %s", ex.getMessage());
        return RestResponse.status(
                Response.Status.CONFLICT,
                ApiResponse.error(409, "Dữ liệu đã bị thay đổi bởi người khác, vui lòng thử lại", "OPTIMISTIC_LOCK_ERROR")
        );
    }

    // --- Security: Unauthorized & Forbidden ---

    @ServerExceptionMapper
    public RestResponse<ApiResponse<Void>> handleUnauthorized(io.quarkus.security.UnauthorizedException ex) {
        return RestResponse.status(
                Response.Status.UNAUTHORIZED,
                ApiResponse.unauthorized("Bạn cần đăng nhập để thực hiện thao tác này")
        );
    }

    @ServerExceptionMapper
    public RestResponse<ApiResponse<Void>> handleForbidden(io.quarkus.security.ForbiddenException ex) {
        return RestResponse.status(
                Response.Status.FORBIDDEN,
                ApiResponse.forbidden("Bạn không có quyền thực hiện thao tác này")
        );
    }

    // --- Framework Exceptions (like 400 Bad Request from malformed JSON or 404) ---
    
    @ServerExceptionMapper
    public RestResponse<ApiResponse<Void>> handleWebApplicationException(jakarta.ws.rs.WebApplicationException ex) {
        LOG.warnf("Web Application Exception: %s", ex.getMessage());
        return RestResponse.ResponseBuilder.<ApiResponse<Void>>create(ex.getResponse().getStatus())
                .entity(ApiResponse.error(ex.getResponse().getStatus(), ex.getMessage(), "HTTP_ERROR"))
                .build();
    }

    // --- Catch-all: Internal Server Error ---

    @ServerExceptionMapper
    public RestResponse<ApiResponse<Void>> handleGenericException(Exception ex) {
        LOG.errorf(ex, "Unexpected error: %s", ex.getMessage());
        return RestResponse.status(
                Response.Status.INTERNAL_SERVER_ERROR,
                ApiResponse.internalError()
        );
    }
}
