package com.monowear.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monowear.entity.Order;
import com.monowear.entity.enums.OrderStatus;
import com.monowear.exception.BadRequestException;
import com.monowear.repository.OrderRepository;
import com.monowear.repository.SkuRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
@Slf4j
public class PayOsService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final OrderRepository orderRepository;
    private final SkuRepository skuRepository;

    @Value("${payos.api-url}") private String apiUrl;
    @Value("${payos.client-id}") private String clientId;
    @Value("${payos.api-key}") private String apiKey;
    @Value("${payos.checksum-key}") private String checksumKey;
    @Value("${payos.return-url}") private String returnUrl;
    @Value("${payos.cancel-url}") private String cancelUrl;

    public PayOsService(OrderRepository orderRepository, SkuRepository skuRepository) {
        this.orderRepository = orderRepository;
        this.skuRepository = skuRepository;
    }

    public Map<String, String> createPaymentLink(Long orderId, long amount) {
        try {
            validateConfig();
            long orderCode = orderId;
            String description = "MONOWEAR DH" + orderId;

            TreeMap<String, String> checksumData = new TreeMap<>();
            checksumData.put("amount", String.valueOf(amount));
            checksumData.put("cancelUrl", cancelUrl);
            checksumData.put("description", description);
            checksumData.put("orderCode", String.valueOf(orderCode));
            checksumData.put("returnUrl", returnUrl);

            StringBuilder rawData = new StringBuilder();
            for (Map.Entry<String, String> entry : checksumData.entrySet()) {
                if (!rawData.isEmpty()) rawData.append("&");
                rawData.append(entry.getKey()).append("=").append(entry.getValue());
            }
            String checksum = hmacSHA256(checksumKey, rawData.toString());

            var body = MAPPER.createObjectNode();
            body.put("orderCode", orderCode);
            body.put("amount", amount);
            body.put("description", description);
            body.put("cancelUrl", cancelUrl);
            body.put("returnUrl", returnUrl);

            var item = MAPPER.createObjectNode();
            item.put("name", "Thanh toán đơn hàng #" + orderId);
            item.put("quantity", 1);
            item.put("price", amount);
            var items = MAPPER.createArrayNode();
            items.add(item);
            body.set("items", items);
            body.put("signature", checksum);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(20))
                    .header("x-client-id", clientId)
                    .header("x-api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = MAPPER.readTree(response.body());

            log.info("payOS response: {}", response.body());

            String code = json.path("code").asText("");
            if (!"00".equals(code)) {
                String msg = json.path("desc").asText("Không thể tạo link thanh toán payOS");
                throw new BadRequestException("payOS error: " + msg);
            }

            JsonNode data = json.path("data");
            Map<String, String> result = new HashMap<>();
            result.put("checkoutUrl", data.path("checkoutUrl").asText(""));
            result.put("paymentLinkId", data.path("paymentLinkId").asText(""));
            result.put("qrCode", data.path("qrCode").asText(""));
            result.put("orderId", String.valueOf(orderId));
            result.put("amount", String.valueOf(amount));
            return result;
        } catch (BadRequestException e) { throw e; }
        catch (Exception e) {
            log.error("payOS payment creation failed", e);
            throw new BadRequestException("Không thể tạo thanh toán payOS: " + e.getMessage());
        }
    }

    @Transactional
    public void handleWebhook(Map<String, Object> webhookData) {
        try {
            String code = String.valueOf(webhookData.getOrDefault("code", ""));
            if (!"00".equals(code)) {
                log.warn("payOS webhook: non-success code={}", code);
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) webhookData.get("data");
            if (data == null) return;

            long orderCode = ((Number) data.getOrDefault("orderCode", 0)).longValue();
            String paymentStatus = String.valueOf(data.getOrDefault("code", data.getOrDefault("status", "")));

            Order order = orderRepository.findByIdWithItemsAndSkus(orderCode).orElse(null);
            if (order == null) { log.warn("payOS webhook: Order #{} not found", orderCode); return; }
            if (order.getStatus() != OrderStatus.PENDING) {
                log.info("payOS webhook: Order #{} already processed (status: {})", orderCode, order.getStatus());
                return;
            }

            if ("PAID".equalsIgnoreCase(paymentStatus) || "00".equals(paymentStatus)) {
                order.setStatus(OrderStatus.CONFIRMED);
                log.info("payOS webhook: Order #{} payment confirmed", orderCode);
            } else {
                order.setStatus(OrderStatus.CANCELLED);
                if (order.getItems() != null) {
                    for (var item : order.getItems()) {
                        if (item.getSku() != null) {
                            skuRepository.restoreStock(item.getSku().getId(), item.getQuantity());
                        }
                    }
                }
                log.warn("payOS webhook: Order #{} cancelled (status: {})", orderCode, paymentStatus);
            }
        } catch (Exception e) {
            log.error("payOS webhook processing error", e);
        }
    }

    private void validateConfig() {
        if (clientId == null || clientId.isBlank() || apiKey == null || apiKey.isBlank()) {
            throw new BadRequestException("Thiếu cấu hình payOS");
        }
    }

    private String hmacSHA256(String key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
