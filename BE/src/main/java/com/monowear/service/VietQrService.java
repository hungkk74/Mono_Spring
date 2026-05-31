package com.monowear.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monowear.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class VietQrService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${vietqr.api-url}") private String apiUrl;
    @Value("${vietqr.banks-url:https://api.vietqr.io/v2/banks}") private String banksUrl;
    @Value("${vietqr.lookup-url:https://api.vietqr.io/v2/lookup}") private String lookupUrl;
    @Value("${vietqr.lookup-enabled:true}") private boolean lookupEnabled;
    @Value("${vietqr.client-id}") private String clientId;
    @Value("${vietqr.api-key}") private String apiKey;
    @Value("${vietqr.account-no}") private String accountNo;
    @Value("${vietqr.account-name}") private String accountName;
    @Value("${vietqr.bank-code:MB}") private String bankCode;
    @Value("${vietqr.acq-id:}") private String fallbackAcqId;
    @Value("${vietqr.template}") private String template;

    public Map<String, String> generateQr(Long orderId, long amount) {
        validatePaymentInput(orderId, amount);
        try {
            String addInfo = "MONOWEAR DH" + orderId;
            BankInfo bank = resolveBankInfo();
            String verifiedAccountName = resolveAccountName(bank);

            if (!hasApiCredentials()) {
                log.warn("VietQR client id/api key is not configured. Falling back to VietQR image URL.");
                return buildFallbackQr(orderId, amount, addInfo, bank, verifiedAccountName);
            }

            var body = MAPPER.createObjectNode();
            body.put("accountNo", accountNo.trim());
            body.put("accountName", verifiedAccountName);
            body.put("acqId", bank.bin());
            body.put("addInfo", addInfo);
            body.put("amount", String.valueOf(amount));
            body.put("template", template.trim());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(20))
                    .header("x-client-id", clientId.trim())
                    .header("x-api-key", apiKey.trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = MAPPER.readTree(response.body());
            JsonNode data = json.path("data");
            String qrDataUrl = data.path("qrDataURL").asText("");
            String qrCode = data.path("qrCode").asText("");

            if (response.statusCode() >= 400 || qrDataUrl.isBlank()) {
                String message = json.path("desc").asText("Khong the tao ma VietQR");
                throw new BadRequestException("VietQR error: " + message);
            }

            log.info("VietQR generated for Order #{}: bank={}, bin={}, account={}, amount={}",
                    orderId, bank.displayName(), bank.bin(), maskAccountNo(accountNo), amount);

            return buildResult(orderId, amount, addInfo, bank, verifiedAccountName, qrDataUrl, qrCode);
        } catch (BadRequestException e) { throw e; }
        catch (Exception e) {
            log.error("VietQR generation failed", e);
            throw new BadRequestException("Khong the tao ma VietQR: " + e.getMessage());
        }
    }

    private String resolveAccountName(BankInfo bank) {
        if (!lookupEnabled || !hasApiCredentials()) return accountName.trim();
        try {
            var body = MAPPER.createObjectNode();
            body.put("bin", Integer.parseInt(bank.bin()));
            body.put("accountNumber", accountNo.trim());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(lookupUrl))
                    .timeout(Duration.ofSeconds(20))
                    .header("x-client-id", clientId.trim())
                    .header("x-api-key", apiKey.trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = MAPPER.readTree(response.body());
            String code = json.path("code").asText("");
            String resolvedName = json.path("data").path("accountName").asText("");

            if (response.statusCode() >= 400 || !"00".equals(code) || resolvedName.isBlank()) {
                String message = json.path("desc").asText("Tai khoan ngan hang khong ton tai");
                throw new BadRequestException("Tai khoan " + bank.displayName() + " " + maskAccountNo(accountNo) + " khong hop le: " + message);
            }
            return resolvedName;
        } catch (BadRequestException e) { throw e; }
        catch (NumberFormatException e) { throw new BadRequestException("Ma BIN ngan hang VietQR khong hop le: " + bank.bin()); }
        catch (Exception e) { throw new BadRequestException("Khong the xac thuc tai khoan VietQR: " + e.getMessage()); }
    }

    private BankInfo resolveBankInfo() {
        String configuredBank = normalized(bankCode);
        if (configuredBank.isBlank()) return fallbackBankInfo();
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(banksUrl)).timeout(Duration.ofSeconds(15)).GET().build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = MAPPER.readTree(response.body());
            JsonNode banks = json.path("data");

            if (response.statusCode() >= 400 || !banks.isArray()) {
                throw new BadRequestException("VietQR bank list error: " + json.path("desc").asText(""));
            }

            for (JsonNode b : banks) {
                String code = b.path("code").asText("");
                String shortName = b.path("shortName").asText(b.path("short_name").asText(""));
                String bin = b.path("bin").asText("");
                if (matchesConfiguredBank(configuredBank, code, shortName, bin)) {
                    int transferSupported = b.path("transferSupported").asInt(b.path("isTransfer").asInt(1));
                    if (transferSupported != 1) throw new BadRequestException("Ngan hang " + displayName(code, shortName) + " chua ho tro VietQR");
                    return new BankInfo(bin, code, shortName, b.path("name").asText(displayName(code, shortName)));
                }
            }
            throw new BadRequestException("Khong tim thay ngan hang VietQR: " + bankCode);
        } catch (BadRequestException e) { throw e; }
        catch (Exception e) {
            BankInfo fallback = fallbackBankInfo();
            log.warn("Cannot load VietQR bank list. Using fallback bin {} for bank {}", fallback.bin(), fallback.displayName());
            return fallback;
        }
    }

    private BankInfo fallbackBankInfo() {
        if (fallbackAcqId == null || fallbackAcqId.isBlank()) throw new BadRequestException("Thieu cau hinh VietQR bank-code/acq-id");
        String code = bankCode == null || bankCode.isBlank() ? "CONFIGURED_BANK" : bankCode.trim();
        return new BankInfo(fallbackAcqId.trim(), code, code, code);
    }

    private Map<String, String> buildFallbackQr(Long orderId, long amount, String addInfo, BankInfo bank, String verifiedAccountName) {
        String encodedAddInfo = URLEncoder.encode(addInfo, StandardCharsets.UTF_8);
        String encodedAccountName = URLEncoder.encode(verifiedAccountName, StandardCharsets.UTF_8);
        String qrDataUrl = String.format("https://img.vietqr.io/image/%s-%s-%s.png?amount=%d&addInfo=%s&accountName=%s",
                bank.bin(), accountNo.trim(), template.trim(), amount, encodedAddInfo, encodedAccountName);
        return buildResult(orderId, amount, addInfo, bank, verifiedAccountName, qrDataUrl, "");
    }

    private Map<String, String> buildResult(Long orderId, long amount, String addInfo, BankInfo bank, String verifiedAccountName, String qrDataUrl, String qrCode) {
        Map<String, String> result = new HashMap<>();
        result.put("qrDataURL", qrDataUrl); result.put("qrCode", qrCode);
        result.put("accountNo", accountNo.trim()); result.put("accountName", verifiedAccountName);
        result.put("bankBin", bank.bin()); result.put("bankCode", bank.code());
        result.put("bankName", bank.displayName()); result.put("acqId", bank.bin());
        result.put("amount", String.valueOf(amount)); result.put("addInfo", addInfo);
        result.put("orderId", String.valueOf(orderId));
        return result;
    }

    private void validatePaymentInput(Long orderId, long amount) {
        if (orderId == null || orderId <= 0) throw new BadRequestException("Ma don hang khong hop le");
        if (amount <= 0) throw new BadRequestException("So tien thanh toan khong hop le");
        if (isBlank(accountNo) || isBlank(accountName) || isBlank(template)) throw new BadRequestException("Thieu cau hinh tai khoan VietQR");
    }

    private boolean hasApiCredentials() { return !isBlank(clientId) && !clientId.contains("CLIENT_ID_HERE") && !isBlank(apiKey) && !apiKey.contains("API_KEY_HERE"); }
    private boolean matchesConfiguredBank(String configuredBank, String code, String shortName, String bin) { return configuredBank.equals(normalized(code)) || configuredBank.equals(normalized(shortName)) || configuredBank.equals(normalized(bin)); }
    private String normalized(String value) { return value == null ? "" : value.replaceAll("[\\s_-]", "").toUpperCase(); }
    private String displayName(String code, String shortName) { if (!isBlank(shortName)) return shortName; if (!isBlank(code)) return code; return "VietQR Bank"; }
    private String maskAccountNo(String value) { if (isBlank(value) || value.length() <= 4) return "****"; return "****" + value.substring(value.length() - 4); }
    private boolean isBlank(String value) { return value == null || value.isBlank(); }
    private record BankInfo(String bin, String code, String shortName, String name) { String displayName() { if (shortName != null && !shortName.isBlank()) return shortName; if (name != null && !name.isBlank()) return name; return code; } }
}
