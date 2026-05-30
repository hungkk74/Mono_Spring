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
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class MomoService {

    private static final Logger LOG = Logger.getLogger(MomoService.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @ConfigProperty(name = "momo.partner-code")
    String partnerCode;

    @ConfigProperty(name = "momo.access-key")
    String accessKey;

    @ConfigProperty(name = "momo.secret-key")
    String secretKey;

    @ConfigProperty(name = "momo.api-url")
    String apiUrl;

    @ConfigProperty(name = "momo.redirect-url")
    String redirectUrl;

    @ConfigProperty(name = "momo.ipn-url")
    String ipnUrl;

    /**
     * Tạo yêu cầu thanh toán MoMo cho đơn hàng.
     * @param requestType — "payWithMethod" (QR), "payWithATM" (ATM), "payWithCC" (Visa/Mastercard)
     * @return payUrl — URL redirect sang trang thanh toán MoMo.
     */
    public String createPayment(Long orderId, long amount, String orderInfo, String requestType) {
        try {
            String requestId = partnerCode + System.currentTimeMillis();
            String momoOrderId = "MONO-" + orderId + "-" + System.currentTimeMillis();
            String extraData = String.valueOf(orderId);

            // Build raw signature
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

            // Build request body
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
            LOG.infof("MoMo create payment request for Order #%d, amount: %d", orderId, amount);

            // Send HTTP POST
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode jsonResponse = mapper.readTree(response.body());

            LOG.infof("MoMo response: %s", response.body());

            int resultCode = jsonResponse.get("resultCode").asInt();
            if (resultCode == 0) {
                return jsonResponse.get("payUrl").asText();
            } else {
                String msg = jsonResponse.has("message") ? jsonResponse.get("message").asText() : "Unknown error";
                throw new BadRequestException("MoMo error: " + msg);
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("MoMo payment creation failed", e);
            throw new BadRequestException("Không thể tạo thanh toán MoMo: " + e.getMessage());
        }
    }

    /**
     * Xác minh chữ ký IPN callback từ MoMo.
     */
    public boolean verifySignature(String rawSignature, String signature) {
        try {
            String computed = hmacSHA256(secretKey, rawSignature);
            return computed.equals(signature);
        } catch (Exception e) {
            LOG.error("Signature verification failed", e);
            return false;
        }
    }

    /**
     * Xử lý IPN callback — cập nhật trạng thái đơn hàng.
     */
    @Transactional
    public void handleIpn(int resultCode, String extraData, String momoOrderId, String signature, Map<String, Object> params) {
        try {
            // Verify signature if provided (typically from MoMo Server-to-Server IPN)
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

                LOG.infof("DEBUG MoMo verification: rawSig=[%s], signature=[%s]", rawSig, signature);

                boolean isSignatureValid = verifySignature(rawSig, signature);
                LOG.infof("DEBUG MoMo verification result: %b", isSignatureValid);

                if (!isSignatureValid) {
                    LOG.error("MoMo IPN signature verification failed! Possible fraud attempt.");
                    throw new BadRequestException("Invalid signature", "INVALID_SIGNATURE");
                }
            } else {
                LOG.warn("MoMo handleIpn called without signature verification (likely fallback dev callback)");
            }

            Long orderId = null;

            // Try to extract from momoOrderId first: MONO-{id}-{timestamp}
            if (momoOrderId != null && momoOrderId.startsWith("MONO-")) {
                String[] parts = momoOrderId.split("-");
                if (parts.length >= 2) {
                    try {
                        orderId = Long.parseLong(parts[1]);
                    } catch (NumberFormatException ignored) {}
                }
            }

            // Fallback to extraData
            if (orderId == null && extraData != null && !extraData.isEmpty()) {
                try {
                    // If MoMo base64 encodes extraData, it won't be a simple number
                    if (!extraData.matches("\\d+")) {
                        extraData = new String(java.util.Base64.getDecoder().decode(extraData));
                    }
                    orderId = Long.parseLong(extraData);
                } catch (Exception ignored) {}
            }

            if (orderId == null) {
                LOG.errorf("MoMo IPN: Cannot determine orderId from extraData=%s, momoOrderId=%s", extraData, momoOrderId);
                return;
            }

            // Eager-fetch order with items and SKUs to avoid LazyInitializationException
            Order order = Order.find(
                "SELECT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.sku WHERE o.id = ?1",
                orderId
            ).firstResult();

            if (order == null) {
                LOG.warnf("MoMo IPN: Order #%d not found", orderId);
                return;
            }

            // Avoid re-processing already handled orders
            if (order.status != OrderStatus.PENDING) {
                LOG.infof("MoMo IPN: Order #%d already processed (status: %s)", orderId, order.status);
                return;
            }

            if (resultCode == 0) {
                order.status = OrderStatus.CONFIRMED;
                LOG.infof("MoMo IPN: Order #%d payment confirmed", orderId);
            } else {
                order.status = OrderStatus.CANCELLED;
                if (order.items != null) {
                    for (var item : order.items) {
                        if (item.sku != null) {
                            Sku.restoreStock(item.sku.id, item.quantity);
                        }
                    }
                }
                LOG.warnf("MoMo IPN: Order #%d cancelled (payment failed, code: %d)", orderId, resultCode);
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("MoMo IPN: Error processing IPN", e);
        }
    }

    // --- Helper ---
    private String hmacSHA256(String key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
