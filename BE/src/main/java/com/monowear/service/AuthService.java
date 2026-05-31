package com.monowear.service;

import com.monowear.config.JwtService;
import com.monowear.dto.auth.*;
import com.monowear.entity.User;
import com.monowear.entity.enums.UserRole;
import com.monowear.exception.BadRequestException;
import com.monowear.exception.DuplicateResourceException;
import com.monowear.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final int OTP_TTL_MINUTES = 5;
    private static final long RESET_TOKEN_LIFESPAN = 900L; // 15 minutes
    private static final String RESET_PURPOSE_CLAIM = "password_reset";

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final OtpStore otpStore;
    private final ResetTokenBlacklist resetTokenBlacklist;
    private final EmailService emailService;

    @org.springframework.beans.factory.annotation.Value("${firebase.api-key:AIzaSyDZr-k5hCgQPCwN3-n_WIx5nCKkdFJenq0}")
    private String firebaseApiKey;

    /**
     * Đăng ký tài khoản mới (CUSTOMER).
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email().trim().toLowerCase()).isPresent()) {
            throw new DuplicateResourceException("Email [" + request.email() + "] đã được sử dụng");
        }

        User user = new User();
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.CUSTOMER);
        user.setFullName(request.fullName().trim());
        user.setPhoneNumber(request.phoneNumber());
        user.setAddress(request.address());
        user.setIsActive(true);
        userRepository.save(user);

        log.info("User registered: {} (ID: {})", user.getEmail(), user.getId());

        String token = generateToken(user);
        return AuthResponse.of(token, jwtService.getExpirationSeconds(), UserResponse.from(user));
    }

    /**
     * Đăng nhập bằng email + password.
     */
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new BadRequestException("Email hoặc mật khẩu không đúng", "INVALID_CREDENTIALS"));

        if (!user.getIsActive()) {
            throw new BadRequestException("Tài khoản đã bị vô hiệu hóa", "ACCOUNT_DISABLED");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadRequestException("Email hoặc mật khẩu không đúng", "INVALID_CREDENTIALS");
        }

        log.info("User logged in: {} (ID: {})", user.getEmail(), user.getId());

        String token = generateToken(user);
        return AuthResponse.of(token, jwtService.getExpirationSeconds(), UserResponse.from(user));
    }

    /**
     * Đăng nhập / Đăng ký tự động bằng tài khoản Google.
     */
    public AuthResponse loginWithGoogle(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new BadRequestException("Google ID Token không được để trống", "GOOGLE_TOKEN_REQUIRED");
        }

        // 1. Xác thực ID Token qua Google API
        java.util.Map<String, Object> claims = verifyGoogleIdToken(idToken);
        String email = (String) claims.get("email");
        String name = (String) claims.get("name");

        if (email == null || email.isBlank()) {
            throw new BadRequestException("Không thể lấy thông tin email từ tài khoản Google", "GOOGLE_EMAIL_NOT_FOUND");
        }

        String normalized = email.trim().toLowerCase();

        // 2. Tìm kiếm hoặc tự động đăng ký khách hàng mới
        User user = userRepository.findByEmail(normalized).orElse(null);
        if (user == null) {
            user = new User();
            user.setEmail(normalized);
            user.setFullName(name != null && !name.isBlank() ? name.trim() : "Người dùng Google");
            user.setRole(UserRole.CUSTOMER);
            user.setPasswordHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
            user.setIsActive(true);
            userRepository.save(user);
            log.info("Google user auto-registered: {} (ID: {})", user.getEmail(), user.getId());
        } else {
            if (!user.getIsActive()) {
                throw new BadRequestException("Tài khoản đã bị vô hiệu hóa", "ACCOUNT_DISABLED");
            }
            log.info("Google user logged in: {} (ID: {})", user.getEmail(), user.getId());
        }

        // 3. Tạo JWT access token và phản hồi
        String token = generateToken(user);
        return AuthResponse.of(token, jwtService.getExpirationSeconds(), UserResponse.from(user));
    }

    @SuppressWarnings("unchecked")
    private java.util.Map<String, Object> verifyGoogleIdToken(String idToken) {
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            String payload = String.format("{\"idToken\":\"%s\"}", idToken.replace("\"", "\\\""));

            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://identitytoolkit.googleapis.com/v1/accounts:lookup?key=" + firebaseApiKey))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(payload, java.nio.charset.StandardCharsets.UTF_8))
                    .timeout(java.time.Duration.ofSeconds(10))
                    .build();

            java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("Firebase ID token verification failed with status {}: {}", response.statusCode(), response.body());
                throw new BadRequestException("Mã xác thực Google không hợp lệ hoặc đã hết hạn", "GOOGLE_TOKEN_INVALID");
            }

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String, Object> result = mapper.readValue(response.body(), java.util.Map.class);

            java.util.List<java.util.Map<String, Object>> users = (java.util.List<java.util.Map<String, Object>>) result.get("users");
            if (users == null || users.isEmpty()) {
                throw new BadRequestException("Không tìm thấy thông tin tài khoản", "GOOGLE_USER_NOT_FOUND");
            }

            java.util.Map<String, Object> userClaims = users.get(0);
            return java.util.Map.of(
                "email", userClaims.getOrDefault("email", ""),
                "name", userClaims.getOrDefault("displayName", "")
            );
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error verifying Google Firebase ID Token", e);
            throw new BadRequestException("Không thể xác thực tài khoản Google vào lúc này", "GOOGLE_AUTH_ERROR");
        }
    }

    /**
     * Lấy thông tin user hiện tại từ JWT subject (userId).
     */
    public UserResponse getCurrentUser(String userId) {
        User user = userRepository.findById(Long.parseLong(userId)).orElse(null);
        if (user == null || !user.getIsActive()) {
            throw new BadRequestException("Tài khoản không tồn tại hoặc đã bị vô hiệu hóa");
        }
        return UserResponse.from(user);
    }

    /**
     * Cập nhật hồ sơ cá nhân (Customer tự update).
     */
    @Transactional
    public UserResponse updateProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.findById(Long.parseLong(userId)).orElse(null);
        if (user == null || !user.getIsActive()) {
            throw new BadRequestException("Tài khoản không tồn tại hoặc đã bị vô hiệu hóa");
        }

        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName().trim());
        }
        if (request.phoneNumber() != null) {
            user.setPhoneNumber(request.phoneNumber().trim());
        }
        if (request.address() != null) {
            user.setAddress(request.address().trim());
        }

        log.info("Profile updated for User {}", user.getId());
        return UserResponse.from(user);
    }

    // ===================== FORGOT PASSWORD FLOW =====================

    /**
     * Bước 1: Sinh OTP 6 số, lưu in-memory (TTL 5 phút), gửi email qua Resend.
     */
    public void requestOtp(String email) {
        String normalized = email.trim().toLowerCase();
        User user = userRepository.findByEmail(normalized).orElse(null);
        if (user == null || !user.getIsActive()) {
            log.warn("OTP requested for unknown/inactive email: {}", normalized);
            return;
        }
        String otp = generateOtp();
        otpStore.save(normalized, otp, OTP_TTL_MINUTES);
        emailService.sendOtpEmail(normalized, user.getFullName(), otp, OTP_TTL_MINUTES);
        log.info("OTP sent to {}", normalized);
    }

    /**
     * Bước 2: Xác thực OTP → xóa OTP khỏi store → trả về Reset Token (JWT 15 phút).
     */
    public ResetTokenResponse verifyOtp(String email, String code) {
        String normalized = email.trim().toLowerCase();
        if (!otpStore.verify(normalized, code)) {
            throw new BadRequestException("Mã OTP không hợp lệ hoặc đã hết hạn", "OTP_INVALID");
        }
        otpStore.remove(normalized);

        User user = userRepository.findByEmail(normalized)
                .orElseThrow(() -> new BadRequestException("Tài khoản không tồn tại"));

        String resetToken = jwtService.generateResetToken(user.getId(), normalized, RESET_TOKEN_LIFESPAN);

        log.info("Reset token issued for user {}", user.getId());
        return ResetTokenResponse.of(resetToken, RESET_TOKEN_LIFESPAN);
    }

    /**
     * Bước 3: Xác minh Reset Token → đổi mật khẩu → blacklist token.
     */
    @Transactional
    public void resetPassword(String resetToken, String newPassword) {
        try {
            Claims claims = jwtService.parseToken(resetToken);

            String purpose = claims.get("purpose", String.class);
            if (!RESET_PURPOSE_CLAIM.equals(purpose)) {
                throw new BadRequestException("Reset token không hợp lệ", "RESET_TOKEN_INVALID");
            }

            String jti = claims.getId();
            if (resetTokenBlacklist.isBlacklisted(jti)) {
                throw new BadRequestException("Reset token đã được sử dụng", "RESET_TOKEN_USED");
            }

            Long userId = Long.parseLong(claims.getSubject());
            User user = userRepository.findById(userId).orElse(null);
            if (user == null || !user.getIsActive()) {
                throw new BadRequestException("Tài khoản không tồn tại hoặc đã bị vô hiệu hóa");
            }

            user.setPasswordHash(passwordEncoder.encode(newPassword));

            long exp = claims.getExpiration().getTime() / 1000;
            resetTokenBlacklist.invalidate(jti, exp);

            log.info("Password reset successful for user {}", userId);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Failed to parse reset token: {}", e.getMessage());
            throw new BadRequestException("Reset token không hợp lệ hoặc đã hết hạn", "RESET_TOKEN_INVALID");
        }
    }

    // ===================== PRIVATE HELPERS =====================

    private String generateToken(User user) {
        return jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name(), user.getFullName());
    }

    /**
     * Public wrapper — dùng cho UserService tạo staff.
     */
    public String hashPasswordPublic(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }

    private String generateOtp() {
        int otp = new java.security.SecureRandom().nextInt(900000) + 100000;
        return String.valueOf(otp);
    }
}
