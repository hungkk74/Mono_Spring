package com.monowear.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monowear.entity.Order;
import com.monowear.entity.Sku;
import com.monowear.entity.enums.OrderStatus;
import com.monowear.exception.BadRequestException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

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

@ApplicationScoped
public class PayOsService {

    private static final Logger LOG = Logger.getLogger(PayOsService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @ConfigProperty(name = "payos.api-url")
    String apiUrl;

    @ConfigProperty(name = "payos.client-id")
    String clientId;

    @ConfigProperty(name = "payos.api-key")
    String apiKey;

    @ConfigProperty(name = "payos.checksum-key")
    String checksumKey;

    @ConfigProperty(name = "payos.return-url")
    String returnUrl;

    @ConfigProperty(name = "payos.cancel-url")
    String cancelUrl;

    public Map<String, String> createPaymentLink(Long orderId, long amount) {
        try {
            validateConfig();

            long orderCode = orderId;
            String description = "MONOWEAR DH" + orderId;

            Map<String, String> signatureData = new TreeMap<>();
            signatureData.put("amount", String.valueOf(amount));
            signatureData.put("cancelUrl", cancelUrl);
            signatureData.put("description", description);
            signatureData.put("orderCode", String.valueOf(orderCode));
            signatureData.put("returnUrl", returnUrl);

            var body = MAPPER.createObjectNode();
            body.put("orderCode", orderCode);
            body.put("amount", amount);
            body.put("description", description);
            body.put("cancelUrl", cancelUrl);
            body.put("returnUrl", returnUrl);
            body.put("signature", sign(signatureData));

            HttpResponse<String> response = sendPost(apiUrl, MAPPER.writeValueAsString(body));
            JsonNode json = MAPPER.readTree(response.body());
            String code = json.path("code").asText("");

            if (response.statusCode() >= 400 || !"00".equals(code)) {
                String message = json.path("desc").asText("Khong the tao payment link payOS");
                throw new BadRequestException("payOS error: " + message);
            }

            JsonNode data = json.path("data");
            String checkoutUrl = data.path("checkoutUrl").asText("");
            if (checkoutUrl.isBlank()) {
                throw new BadRequestException("payOS khong tra ve checkoutUrl");
            }

            Map<String, String> result = new HashMap<>();
            result.put("checkoutUrl", checkoutUrl);
            result.put("paymentLinkId", data.path("paymentLinkId").asText(""));
            result.put("orderCode", String.valueOf(orderCode));
            result.put("qrCode", data.path("qrCode").asText(""));
            result.put("amount", String.valueOf(amount));
            return result;
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("payOS create payment link failed", e);
            throw new BadRequestException("Khong the tao thanh toan payOS: " + e.getMessage());
        }
    }

    @Transactional
    public void syncPaymentStatus(Long orderCode) {
        try {
            validateConfig();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "/" + orderCode))
                    .timeout(Duration.ofSeconds(20))
                    .header("x-client-id", clientId.trim())
                    .header("x-api-key", apiKey.trim())
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = MAPPER.readTree(response.body());
            String code = json.path("code").asText("");

            if (response.statusCode() >= 400 || !"00".equals(code)) {
                String message = json.path("desc").asText("Khong the kiem tra trang thai payOS");
                throw new BadRequestException("payOS error: " + message);
            }

            JsonNode data = json.path("data");
            String status = data.path("status").asText("");
            Long orderId = data.path("orderCode").asLong(orderCode);

            Order order = findOrderForPayment(orderId);
            if (order == null) {
                LOG.warnf("payOS callback: Order #%d not found", orderId);
                return;
            }
            if (order.status != OrderStatus.PENDING) {
                LOG.infof("payOS callback: Order #%d already processed (%s)", orderId, order.status);
                return;
            }

            if ("PAID".equalsIgnoreCase(status)) {
                order.status = OrderStatus.CONFIRMED;
                LOG.infof("payOS callback: Order #%d payment confirmed", orderId);
                return;
            }

            if ("CANCELLED".equalsIgnoreCase(status) || "EXPIRED".equalsIgnoreCase(status)) {
                order.status = OrderStatus.CANCELLED;
                restoreStock(order);
                LOG.warnf("payOS callback: Order #%d cancelled, status=%s", orderId, status);
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("payOS sync payment status failed", e);
            throw new BadRequestException("Khong the cap nhat trang thai payOS: " + e.getMessage());
        }
    }

    @Transactional
    public void handleWebhook(Map<String, Object> body) {
        Object data = body.get("data");
        if (data == null) {
            LOG.warnf("payOS webhook ignored: missing data, body=%s", body);
            return;
        }

        JsonNode dataNode = MAPPER.valueToTree(data);
        Long orderCode = dataNode.path("orderCode").asLong(0);
        if (orderCode <= 0) {
            LOG.warnf("payOS webhook ignored: invalid orderCode, body=%s", body);
            return;
        }

        syncPaymentStatus(orderCode);
    }

    private HttpResponse<String> sendPost(String url, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("x-client-id", clientId.trim())
                .header("x-api-key", apiKey.trim())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private Order findOrderForPayment(Long orderId) {
        return Order.find(
                "SELECT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.sku WHERE o.id = ?1",
                orderId
        ).firstResult();
    }

    private void restoreStock(Order order) {
        if (order.items == null) return;
        for (var item : order.items) {
            if (item.sku != null) {
                Sku.restoreStock(item.sku.id, item.quantity);
            }
        }
    }

    private String sign(Map<String, String> data) throws Exception {
        StringBuilder raw = new StringBuilder();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (!raw.isEmpty()) raw.append("&");
            raw.append(entry.getKey()).append("=").append(entry.getValue());
        }

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(checksumKey.trim().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(raw.toString().getBytes(StandardCharsets.UTF_8));

        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    private void validateConfig() {
        if (isBlank(apiUrl) || isBlank(clientId) || isBlank(apiKey)
                || isBlank(checksumKey) || isBlank(returnUrl) || isBlank(cancelUrl)) {
            throw new BadRequestException("Thieu cau hinh payOS");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
