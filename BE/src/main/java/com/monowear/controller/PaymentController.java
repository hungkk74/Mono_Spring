package com.monowear.controller;

import com.monowear.dto.common.ApiResponse;
import com.monowear.entity.Order;
import com.monowear.exception.BadRequestException;
import com.monowear.repository.OrderRepository;
import com.monowear.service.MomoService;
import com.monowear.service.PayOsService;
import com.monowear.service.VietQrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final MomoService momoService;
    private final VietQrService vietQrService;
    private final PayOsService payOsService;
    private final OrderRepository orderRepository;

    @PostMapping("/momo/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, String>>> createMomoPayment(@RequestBody Map<String, Object> body, Authentication auth) {
        Long orderId = parseLongSafely(body.get("orderId"));
        if (orderId == null) throw new BadRequestException("Mã đơn hàng không hợp lệ");
        String requestType = body.getOrDefault("requestType", "payWithMethod").toString();
        Long userId = Long.parseLong(auth.getName());

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new BadRequestException("Đơn hàng không tồn tại"));
        if (!order.getUser().getId().equals(userId)) throw new BadRequestException("Bạn không có quyền thanh toán đơn hàng này");

        long amount = order.getTotalAmount().longValue();
        String payUrl = momoService.createPayment(orderId, amount, "Mono Wear - Don hang #" + orderId, requestType);
        return ResponseEntity.ok(ApiResponse.success(Map.of("payUrl", payUrl)));
    }

    @PostMapping("/vietqr/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, String>>> createVietQrPayment(@RequestBody Map<String, Object> body, Authentication auth) {
        Long orderId = parseLongSafely(body.get("orderId"));
        if (orderId == null) throw new BadRequestException("Mã đơn hàng không hợp lệ");
        Long userId = Long.parseLong(auth.getName());

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new BadRequestException("Đơn hàng không tồn tại"));
        if (!order.getUser().getId().equals(userId)) throw new BadRequestException("Bạn không có quyền thanh toán đơn hàng này");

        return ResponseEntity.ok(ApiResponse.success(vietQrService.generateQr(orderId, order.getTotalAmount().longValue())));
    }

    @PostMapping("/payos/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, String>>> createPayOsPayment(@RequestBody Map<String, Object> body, Authentication auth) {
        Long orderId = parseLongSafely(body.get("orderId"));
        if (orderId == null) throw new BadRequestException("Mã đơn hàng không hợp lệ");
        Long userId = Long.parseLong(auth.getName());

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new BadRequestException("Don hang khong ton tai"));
        if (!order.getUser().getId().equals(userId)) throw new BadRequestException("Ban khong co quyen thanh toan don hang nay");
        if (!"BANK_TRANSFER".equalsIgnoreCase(order.getPaymentMethod())) throw new BadRequestException("Don hang khong su dung phuong thuc chuyen khoan ngan hang");

        return ResponseEntity.ok(ApiResponse.success(payOsService.createPaymentLink(orderId, order.getTotalAmount().longValue())));
    }

    @PostMapping("/momo/ipn")
    public ResponseEntity<Void> momoIpn(@RequestBody Map<String, Object> body) {
        log.info("MoMo IPN received: {}", body);
        int resultCode = parseIntSafely(body.get("resultCode"), -1);
        String extraData = body.getOrDefault("extraData", "").toString();
        String momoOrderId = body.getOrDefault("orderId", "").toString();
        String signature = body.getOrDefault("signature", "").toString();

        if (signature.trim().isEmpty()) throw new BadRequestException("Signature is required");
        momoService.handleIpn(resultCode, extraData, momoOrderId, signature, body);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/momo/callback")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> momoCallback(@RequestBody Map<String, Object> body, Authentication auth) {
        int resultCode = parseIntSafely(body.get("resultCode"), -1);
        String extraData = body.getOrDefault("extraData", "").toString();
        String momoOrderId = body.getOrDefault("orderId", "").toString();
        String signature = body.getOrDefault("signature", "").toString();

        Long orderId = parseLongSafely(extraData);
        if (orderId == null && momoOrderId.startsWith("MONO-")) {
            String[] parts = momoOrderId.split("-");
            if (parts.length >= 2) orderId = parseLongSafely(parts[1]);
        }

        if (orderId != null) {
            Order order = orderRepository.findById(orderId).orElseThrow(() -> new BadRequestException("Đơn hàng không tồn tại"));
            Long userId = Long.parseLong(auth.getName());
            if (!order.getUser().getId().equals(userId)) throw new BadRequestException("Bạn không có quyền cập nhật đơn hàng này");
        }

        momoService.handleIpn(resultCode, extraData, momoOrderId, signature, body);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/payos/webhook")
    public ResponseEntity<Void> payOsWebhook(@RequestBody Map<String, Object> body) {
        log.info("payOS webhook received: {}", body);
        payOsService.handleWebhook(body);
        return ResponseEntity.noContent().build();
    }


    private Long parseLongSafely(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            String str = value.toString().trim();
            if (str.contains(".")) str = str.substring(0, str.indexOf("."));
            return Long.parseLong(str);
        } catch (Exception e) { return null; }
    }

    private int parseIntSafely(Object value, int defaultValue) {
        if (value == null) return defaultValue;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            String str = value.toString().trim();
            if (str.contains(".")) str = str.substring(0, str.indexOf("."));
            return Integer.parseInt(str);
        } catch (Exception e) { return defaultValue; }
    }
}
