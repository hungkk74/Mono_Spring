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
import java.util.Map;

@Service
@Slf4j
public class MomoService {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final OrderRepository orderRepository;
    private final SkuRepository skuRepository;

    @Value("${momo.partner-code}") private String partnerCode;
    @Value("${momo.access-key}") private String accessKey;
    @Value("${momo.secret-key}") private String secretKey;
    @Value("${momo.api-url}") private String apiUrl;
    @Value("${momo.redirect-url}") private String redirectUrl;
    @Value("${momo.ipn-url}") private String ipnUrl;

    public MomoService(OrderRepository orderRepository, SkuRepository skuRepository) {
        this.orderRepository = orderRepository;
        this.skuRepository = skuRepository;
    }

    public String createPayment(Long orderId, long amount, String orderInfo, String requestType) {
        try {
            String requestId = partnerCode + System.currentTimeMillis();
            String momoOrderId = "MONO-" + orderId + "-" + System.currentTimeMillis();
            String extraData = String.valueOf(orderId);

            String rawSignature = "accessKey=" + accessKey
                    + "&amount=" + amount
                    + "&extraData=" + extraData
                    + "&ipnUrl=" + ipnUrl
                    + "&orderId=" + momoOrderId
                    + "&orderInfo=" + orderInfo
                    + "&partnerCode=" + partnerCode
                    + "&redirectUrl=" + redirectUrl
                    + "&requestId=" + requestId
                    + "&requestType=" + requestType;

            String signature = hmacSHA256(secretKey, rawSignature);

            var body = mapper.createObjectNode();
            body.put("partnerCode", partnerCode);
            body.put("partnerName", "Mono Wear");
            body.put("storeId", "MonoWearStore");
            body.put("requestId", requestId);
            body.put("amount", amount);
            body.put("orderId", momoOrderId);
            body.put("orderInfo", orderInfo);
            body.put("redirectUrl", redirectUrl);
            body.put("ipnUrl", ipnUrl);
            body.put("extraData", extraData);
            body.put("requestType", requestType);
            body.put("autoCapture", true);
            body.put("orderGroupId", "");
            body.put("signature", signature);
            body.put("lang", "vi");

            String requestBody = mapper.writeValueAsString(body);
            log.info("MoMo create payment request for Order #{}, amount: {}", orderId, amount);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode jsonResponse = mapper.readTree(response.body());

            log.info("MoMo response: {}", response.body());

            int resultCode = jsonResponse.get("resultCode").asInt();
            if (resultCode == 0) {
                return jsonResponse.get("payUrl").asText();
            } else {
                String msg = jsonResponse.has("message") ? jsonResponse.get("message").asText() : "Unknown error";
                throw new BadRequestException("MoMo error: " + msg);
            }
        } catch (BadRequestException e) { throw e; }
        catch (Exception e) {
            log.error("MoMo payment creation failed", e);
            throw new BadRequestException("Không thể tạo thanh toán MoMo: " + e.getMessage());
        }
    }

    public boolean verifySignature(String rawSignature, String signature) {
        try {
            String computed = hmacSHA256(secretKey, rawSignature);
            return computed.equals(signature);
        } catch (Exception e) {
            log.error("Signature verification failed", e);
            return false;
        }
    }

    @Transactional
    public void handleIpn(int resultCode, String extraData, String momoOrderId, String signature, Map<String, Object> params) {
        try {
            if (signature != null && !signature.isEmpty() && params != null && params.containsKey("signature")) {
                String transId = params.getOrDefault("transId", "").toString();
                String message = params.getOrDefault("message", "").toString();
                String partnerCodeVal = params.getOrDefault("partnerCode", "").toString();
                String requestId = params.getOrDefault("requestId", "").toString();
                String responseTime = params.getOrDefault("responseTime", "").toString();
                String payType = params.getOrDefault("payType", "").toString();
                String amount = params.getOrDefault("amount", "").toString();

                String rawSig = "accessKey=" + accessKey
                        + "&amount=" + amount
                        + "&extraData=" + extraData
                        + "&message=" + message
                        + "&momoTransId=" + transId
                        + "&partnerCode=" + partnerCodeVal
                        + "&requestId=" + requestId
                        + "&responseTime=" + responseTime
                        + "&resultCode=" + resultCode
                        + "&payType=" + payType;

                if (!verifySignature(rawSig, signature)) {
                    log.error("MoMo IPN signature verification failed!");
                    throw new BadRequestException("Invalid signature", "INVALID_SIGNATURE");
                }
            }

            Long orderId = null;
            if (momoOrderId != null && momoOrderId.startsWith("MONO-")) {
                String[] parts = momoOrderId.split("-");
                if (parts.length >= 2) {
                    try { orderId = Long.parseLong(parts[1]); } catch (NumberFormatException ignored) {}
                }
            }
            if (orderId == null && extraData != null && !extraData.isEmpty()) {
                try {
                    if (!extraData.matches("\\d+")) {
                        extraData = new String(java.util.Base64.getDecoder().decode(extraData));
                    }
                    orderId = Long.parseLong(extraData);
                } catch (Exception ignored) {}
            }

            if (orderId == null) {
                log.error("MoMo IPN: Cannot determine orderId from extraData={}, momoOrderId={}", extraData, momoOrderId);
                return;
            }

            Order order = orderRepository.findByIdWithItemsAndSkus(orderId).orElse(null);
            if (order == null) { log.warn("MoMo IPN: Order #{} not found", orderId); return; }
            if (order.getStatus() != OrderStatus.PENDING) {
                log.info("MoMo IPN: Order #{} already processed (status: {})", orderId, order.getStatus());
                return;
            }

            if (resultCode == 0) {
                order.setStatus(OrderStatus.CONFIRMED);
                log.info("MoMo IPN: Order #{} payment confirmed", orderId);
            } else {
                order.setStatus(OrderStatus.CANCELLED);
                if (order.getItems() != null) {
                    for (var item : order.getItems()) {
                        if (item.getSku() != null) {
                            skuRepository.restoreStock(item.getSku().getId(), item.getQuantity());
                        }
                    }
                }
                log.warn("MoMo IPN: Order #{} cancelled (payment failed, code: {})", orderId, resultCode);
            }
        } catch (BadRequestException e) { throw e; }
        catch (Exception e) { log.error("MoMo IPN: Error processing IPN", e); }
    }

    private String hmacSHA256(String key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) { sb.append(String.format("%02x", b)); }
        return sb.toString();
    }
}
