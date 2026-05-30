package com.monowear.resource;

import com.monowear.dto.common.ApiResponse;
import com.monowear.entity.Order;
import com.monowear.exception.BadRequestException;
import com.monowear.service.MomoService;
import com.monowear.service.PayOsService;
import com.monowear.service.VietQrService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.Map;

/**
 * Payment APIs — MoMo integration.
 */
@Path("/api/payment")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PaymentResource {

    private static final Logger LOG = Logger.getLogger(PaymentResource.class);

    private Long parseLongSafely(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            String str = value.toString().trim();
            if (str.contains(".")) {
                str = str.substring(0, str.indexOf("."));
            }
            return Long.parseLong(str);
        } catch (Exception e) {
            LOG.error("Failed to parse Long from value: " + value, e);
            return null;
        }
    }

    private int parseIntSafely(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            String str = value.toString().trim();
            if (str.contains(".")) {
                str = str.substring(0, str.indexOf("."));
            }
            return Integer.parseInt(str);
        } catch (Exception e) {
            LOG.error("Failed to parse Integer from value: " + value, e);
            return defaultValue;
        }
    }

    @Inject
    MomoService momoService;

    @Inject
    VietQrService vietQrService;

    @Inject
    PayOsService payOsService;

    @Inject
    JsonWebToken jwt;

    /**
     * POST /api/payment/momo/create — Tạo thanh toán MoMo cho đơn hàng.
     * Body: { "orderId": 123 }
     */
    @POST
    @Path("/momo/create")
    @RolesAllowed({"CUSTOMER", "ADMIN", "STAFF"})
    public RestResponse<ApiResponse<Map<String, String>>> createMomoPayment(Map<String, Object> body) {
        Long orderId = parseLongSafely(body.get("orderId"));
        if (orderId == null) {
            throw new BadRequestException("Mã đơn hàng không hợp lệ");
        }
        String requestType = body.getOrDefault("requestType", "payWithMethod").toString();
        Long userId = Long.parseLong(jwt.getSubject());

        // Validate order belongs to user
        Order order = Order.findById(orderId);
        if (order == null) {
            throw new BadRequestException("Đơn hàng không tồn tại");
        }
        if (!order.user.id.equals(userId)) {
            throw new BadRequestException("Bạn không có quyền thanh toán đơn hàng này");
        }

        long amount = order.totalAmount.longValue();
        String orderInfo = "Mono Wear - Don hang #" + orderId;
        String payUrl = momoService.createPayment(orderId, amount, orderInfo, requestType);

        return RestResponse.ok(ApiResponse.success(Map.of("payUrl", payUrl)));
    }

    /**
     * POST /api/payment/vietqr/create — Tạo mã QR chuyển khoản ngân hàng cho đơn hàng.
     * Body: { "orderId": 123 }
     */
    @POST
    @Path("/vietqr/create")
    @RolesAllowed({"CUSTOMER", "ADMIN", "STAFF"})
    public RestResponse<ApiResponse<Map<String, String>>> createVietQrPayment(Map<String, Object> body) {
        Long orderId = parseLongSafely(body.get("orderId"));
        if (orderId == null) {
            throw new BadRequestException("Mã đơn hàng không hợp lệ");
        }
        Long userId = Long.parseLong(jwt.getSubject());

        Order order = Order.findById(orderId);
        if (order == null) {
            throw new BadRequestException("Đơn hàng không tồn tại");
        }
        if (!order.user.id.equals(userId)) {
            throw new BadRequestException("Bạn không có quyền thanh toán đơn hàng này");
        }

        long amount = order.totalAmount.longValue();
        return RestResponse.ok(ApiResponse.success(vietQrService.generateQr(orderId, amount)));
    }

    @POST
    @Path("/payos/create")
    @RolesAllowed({"CUSTOMER", "ADMIN", "STAFF"})
    public RestResponse<ApiResponse<Map<String, String>>> createPayOsPayment(Map<String, Object> body) {
        Long orderId = parseLongSafely(body.get("orderId"));
        if (orderId == null) {
            throw new BadRequestException("Mã đơn hàng không hợp lệ");
        }
        Long userId = Long.parseLong(jwt.getSubject());

        Order order = Order.findById(orderId);
        if (order == null) {
            throw new BadRequestException("Don hang khong ton tai");
        }
        if (!order.user.id.equals(userId)) {
            throw new BadRequestException("Ban khong co quyen thanh toan don hang nay");
        }
        if (!"BANK_TRANSFER".equalsIgnoreCase(order.paymentMethod)) {
            throw new BadRequestException("Don hang khong su dung phuong thuc chuyen khoan ngan hang");
        }

        long amount = order.totalAmount.longValue();
        return RestResponse.ok(ApiResponse.success(payOsService.createPaymentLink(orderId, amount)));
    }

    /**
     * POST /api/payment/momo/ipn — MoMo IPN callback (server-to-server).
     * Không yêu cầu auth.
     */
    @POST
    @Path("/momo/ipn")
    @PermitAll
    public RestResponse<Void> momoIpn(Map<String, Object> body) {
        LOG.infof("MoMo IPN received: %s", body);

        int resultCode = parseIntSafely(body.get("resultCode"), -1);
        String extraData = body.getOrDefault("extraData", "").toString();
        String momoOrderId = body.getOrDefault("orderId", "").toString();
        String signature = body.getOrDefault("signature", "").toString();

        if (signature == null || signature.trim().isEmpty()) {
            LOG.error("MoMo IPN received without signature! Rejecting request.");
            throw new BadRequestException("Signature is required");
        }

        momoService.handleIpn(resultCode, extraData, momoOrderId, signature, body);

        // MoMo expects HTTP 204 for successful IPN processing
        return RestResponse.noContent();
    }

    /**
     * POST /api/payment/momo/callback — Frontend gọi sau khi MoMo redirect về.
     * Xử lý trường hợp IPN không hoạt động (localhost dev).
     */
    @POST
    @Path("/momo/callback")
    @RolesAllowed({"CUSTOMER", "ADMIN", "STAFF"})
    public RestResponse<ApiResponse<Void>> momoCallback(Map<String, Object> body) {
        int resultCode = parseIntSafely(body.get("resultCode"), -1);
        String extraData = body.getOrDefault("extraData", "").toString();
        String momoOrderId = body.getOrDefault("orderId", "").toString();
        String signature = body.getOrDefault("signature", "").toString();

        Long orderId = parseLongSafely(extraData);
        if (orderId == null && momoOrderId.startsWith("MONO-")) {
            String[] parts = momoOrderId.split("-");
            if (parts.length >= 2) {
                orderId = parseLongSafely(parts[1]);
            }
        }

        if (orderId != null) {
            Order order = Order.findById(orderId);
            if (order == null) {
                throw new BadRequestException("Đơn hàng không tồn tại");
            }
            Long userId = Long.parseLong(jwt.getSubject());
            if (!order.user.id.equals(userId)) {
                throw new BadRequestException("Bạn không có quyền cập nhật đơn hàng này");
            }
        }

        LOG.infof("MoMo callback: resultCode=%d, extraData=%s, orderId=%s", resultCode, extraData, momoOrderId);
        momoService.handleIpn(resultCode, extraData, momoOrderId, signature, body);

        return RestResponse.ok(ApiResponse.success(null));
    }

    @POST
    @Path("/payos/callback")
    @PermitAll
    public RestResponse<ApiResponse<Void>> payOsCallback(Map<String, Object> body) {
        Long orderCode = parseLongSafely(body.get("orderCode"));
        if (orderCode == null) {
            throw new BadRequestException("Mã đơn hàng không hợp lệ");
        }
        payOsService.syncPaymentStatus(orderCode);
        return RestResponse.ok(ApiResponse.success(null));
    }

    @POST
    @Path("/payos/webhook")
    @PermitAll
    public RestResponse<Void> payOsWebhook(Map<String, Object> body) {
        LOG.infof("payOS webhook received: %s", body);
        payOsService.handleWebhook(body);
        return RestResponse.noContent();
    }
}
