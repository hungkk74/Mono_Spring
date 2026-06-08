package com.monowear.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;


@Service
@Slf4j
public class EmailService {

    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${resend.api-key}")
    private String apiKey;

    @Value("${resend.from-email:noreply@monowear.io}")
    private String fromEmail;

    @Value("${resend.from-name:MONO WEAR}")
    private String fromName;

    // Gửi OTP qua email
    @Async
    public void sendOtpEmail(String toEmail, String recipientName, String otpCode, int ttlMinutes) {
        log.info("================================================================");
        log.info("DEVELOPER ALERT: OTP for [{}] is: [ {} ] (Expires in {}m)", toEmail, otpCode, ttlMinutes);
        log.info("================================================================");
        String subject = "Mã xác thực đặt lại mật khẩu - MONO WEAR";
        String html = buildOtpEmailHtml(recipientName, otpCode, ttlMinutes);
        sendEmail(toEmail, subject, html);
    }



    private void sendEmail(String to, String subject, String htmlBody) {
        String fromField = fromName + " <" + fromEmail + ">";
        String payload = String.format(
                "{\"from\":\"%s\",\"to\":[\"%s\"],\"subject\":\"%s\",\"html\":\"%s\"}",
                escapeJson(fromField),
                escapeJson(to),
                escapeJson(subject),
                escapeJson(htmlBody)
        );

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RESEND_API_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Email sent to {} via Resend (status: {})", to, response.statusCode());
            } else {
                log.error("Resend API error [{}]: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
        }
    }

    private String buildOtpEmailHtml(String name, String otp, int ttlMinutes) {
        return "<!DOCTYPE html><html lang='vi'><head><meta charset='UTF-8'>" +
               "<meta name='viewport' content='width=device-width,initial-scale=1'>" +
               "<title>Mã OTP - MONO WEAR</title></head>" +
               "<body style='margin:0;padding:0;background:#f4f4f5;font-family:Arial,sans-serif;'>" +
               "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f4f4f5;padding:40px 0;'>" +
               "<tr><td align='center'>" +
               "<table width='520' cellpadding='0' cellspacing='0' style='background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);'>" +
               "<tr><td style='background:#111;padding:32px 40px;text-align:center;'>" +
               "<h1 style='color:#fff;margin:0;font-size:22px;letter-spacing:4px;font-weight:700;'>MONO WEAR</h1>" +
               "<p style='color:#888;margin:6px 0 0;font-size:12px;letter-spacing:2px;'>PURE MINIMALISM</p>" +
               "</td></tr>" +
               "<tr><td style='padding:40px 40px 20px;'>" +
               "<h2 style='color:#111;font-size:20px;margin:0 0 12px;'>Đặt lại mật khẩu</h2>" +
               "<p style='color:#555;font-size:15px;line-height:1.6;margin:0 0 24px;'>Xin chào <strong>" + escapeHtml(name) + "</strong>,<br>" +
               "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn. " +
               "Sử dụng mã OTP dưới đây:</p>" +
               "<div style='background:#f8f8f8;border:2px dashed #ddd;border-radius:8px;padding:24px;text-align:center;margin:0 0 24px;'>" +
               "<p style='color:#888;font-size:12px;letter-spacing:2px;margin:0 0 8px;text-transform:uppercase;'>Mã xác thực</p>" +
               "<div style='color:#111;font-size:40px;font-weight:900;letter-spacing:12px;font-family:monospace;'>" + otp + "</div>" +
               "<p style='color:#888;font-size:12px;margin:8px 0 0;'>Hiệu lực trong <strong>" + ttlMinutes + " phút</strong></p>" +
               "</div>" +
               "<p style='color:#888;font-size:13px;line-height:1.6;margin:0;'>" +
               "Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này. " +
               "Tài khoản của bạn vẫn an toàn.</p>" +
               "</td></tr>" +
               "<tr><td style='background:#f8f8f8;padding:20px 40px;border-top:1px solid #eee;text-align:center;'>" +
               "<p style='color:#aaa;font-size:12px;margin:0;'>© 2024 MONO WEAR — Không trả lời email này.</p>" +
               "</td></tr>" +
               "</table>" +
               "</td></tr></table></body></html>";
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
