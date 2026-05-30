package com.monowear.service;

import com.monowear.dto.auth.*;
import com.monowear.entity.User;
import com.monowear.entity.enums.UserRole;
import com.monowear.exception.BadRequestException;
import com.monowear.exception.DuplicateResourceException;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.auth.principal.JWTParser;
import org.eclipse.microprofile.jwt.JsonWebToken;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.wildfly.security.password.Password;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.interfaces.BCryptPassword;
import org.wildfly.security.password.util.ModularCrypt;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class AuthService {

    private static final Logger LOG = Logger.getLogger(AuthService.class);
    private static final int OTP_TTL_MINUTES = 5;
    private static final long RESET_TOKEN_LIFESPAN = 900L; // 15 phút
    private static final String RESET_PURPOSE_CLAIM = "password_reset";

    @ConfigProperty(name = "smallrye.jwt.new-token.lifespan", defaultValue = "86400")
    long tokenLifespan;

    @ConfigProperty(name = "mp.jwt.verify.issuer", defaultValue = "https://monowear.io")
    String issuer;

    @Inject
    OtpStore otpStore;

    @Inject
    ResetTokenBlacklist resetTokenBlacklist;

    @Inject
    EmailService emailService;

    @Inject
    JWTParser jwtParser;

    /**
     * Đăng ký tài khoản mới (CUSTOMER).
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check duplicate email
        if (User.findByEmail(request.email()).isPresent()) {
            throw new DuplicateResourceException("Email [" + request.email() + "] đã được sử dụng");
        }

        // Create user
        User user = new User();
        user.email = request.email().trim().toLowerCase();
        user.passwordHash = hashPassword(request.password());
        user.role = UserRole.CUSTOMER;
        user.fullName = request.fullName().trim();
        user.phoneNumber = request.phoneNumber();
        user.address = request.address();
        user.isActive = true;
        user.persist();

        LOG.infof("User registered: %s (ID: %d)", user.email, user.id);

        // Generate token & return
        String token = generateToken(user);
        return AuthResponse.of(token, tokenLifespan, UserResponse.from(user));
    }

    /**
     * Đăng nhập bằng email + password.
     */
    public AuthResponse login(LoginRequest request) {
        User user = User.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new BadRequestException("Email hoặc mật khẩu không đúng", "INVALID_CREDENTIALS"));

        if (!user.isActive) {
            throw new BadRequestException("Tài khoản đã bị vô hiệu hóa", "ACCOUNT_DISABLED");
        }

        if (!verifyPassword(request.password(), user.passwordHash)) {
            throw new BadRequestException("Email hoặc mật khẩu không đúng", "INVALID_CREDENTIALS");
        }

        LOG.infof("User logged in: %s (ID: %d)", user.email, user.id);

        String token = generateToken(user);
        return AuthResponse.of(token, tokenLifespan, UserResponse.from(user));
    }

    /**
     * Lấy thông tin user hiện tại từ JWT subject (userId).
     */
    public UserResponse getCurrentUser(String userId) {
        User user = User.findById(Long.parseLong(userId));
        if (user == null || !user.isActive) {
            throw new BadRequestException("Tài khoản không tồn tại hoặc đã bị vô hiệu hóa");
        }
        return UserResponse.from(user);
    }

    /**
     * Cập nhật hồ sơ cá nhân (Customer tự update).
     */
    @Transactional
    public UserResponse updateProfile(String userId, UpdateProfileRequest request) {
        User user = User.findById(Long.parseLong(userId));
        if (user == null || !user.isActive) {
            throw new BadRequestException("Tài khoản không tồn tại hoặc đã bị vô hiệu hóa");
        }

        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.fullName = request.fullName().trim();
        }
        if (request.phoneNumber() != null) {
            user.phoneNumber = request.phoneNumber().trim();
        }
        if (request.address() != null) {
            user.address = request.address().trim();
        }

        LOG.infof("Profile updated for User %d", user.id);
        return UserResponse.from(user);
    }

    // ===================== FORGOT PASSWORD FLOW =====================

    /**
     * Bước 1: Sinh OTP 6 số, lưu in-memory (TTL 5 phút), gửi email qua Resend.
     * Nếu email không tồn tại → vẫn trả về thành công (tránh email enumeration).
     */
    public void requestOtp(String email) {
        String normalized = email.trim().toLowerCase();
        User user = User.findByEmail(normalized).orElse(null);
        if (user == null || !user.isActive) {
            // Không tiết lộ email có tồn tại hay không
            LOG.warnf("OTP requested for unknown/inactive email: %s", normalized);
            return;
        }
        String otp = generateOtp();
        otpStore.save(normalized, otp, OTP_TTL_MINUTES);
        emailService.sendOtpEmail(normalized, user.fullName, otp, OTP_TTL_MINUTES);
        LOG.infof("OTP sent to %s", normalized);
    }

    /**
     * Bước 2: Xác thực OTP → xóa OTP khỏi store → trả về Reset Token (JWT 15 phút).
     */
    public ResetTokenResponse verifyOtp(String email, String code) {
        String normalized = email.trim().toLowerCase();
        if (!otpStore.verify(normalized, code)) {
            throw new BadRequestException("Mã OTP không hợp lệ hoặc đã hết hạn", "OTP_INVALID");
        }
        // Single-use: xóa ngay sau khi xác thực thành công
        otpStore.remove(normalized);

        User user = User.findByEmail(normalized)
                .orElseThrow(() -> new BadRequestException("Tài khoản không tồn tại"));

        String jti = UUID.randomUUID().toString();
        String resetToken = Jwt.issuer(issuer)
                .subject(String.valueOf(user.id))
                .claim("jti", jti)
                .claim("purpose", RESET_PURPOSE_CLAIM)
                .claim("email", normalized)
                .expiresIn(Duration.ofSeconds(RESET_TOKEN_LIFESPAN))
                .sign();

        LOG.infof("Reset token issued for user %d (jti: %s)", user.id, jti);
        return ResetTokenResponse.of(resetToken, RESET_TOKEN_LIFESPAN);
    }

    /**
     * Bước 3: Xác minh Reset Token → đổi mật khẩu → blacklist token.
     */
    @Transactional
    public void resetPassword(String resetToken, String newPassword) {
        try {
            // Verify signature & parse JWT using SmallRye JWTParser
            JsonWebToken jwt = jwtParser.parse(resetToken);

            String purpose = jwt.getClaim("purpose");
            if (!RESET_PURPOSE_CLAIM.equals(purpose)) {
                throw new BadRequestException("Reset token không hợp lệ", "RESET_TOKEN_INVALID");
            }

            String jti = jwt.getClaim("jti");
            if (resetTokenBlacklist.isBlacklisted(jti)) {
                throw new BadRequestException("Reset token đã được sử dụng", "RESET_TOKEN_USED");
            }

            Long userId = Long.parseLong(jwt.getSubject());
            User user = User.findById(userId);
            if (user == null || !user.isActive) {
                throw new BadRequestException("Tài khoản không tồn tại hoặc đã bị vô hiệu hóa");
            }

            user.passwordHash = hashPassword(newPassword);

            // Invalidate token immediately
            long exp = jwt.getExpirationTime();
            resetTokenBlacklist.invalidate(jti, exp);

            LOG.infof("Password reset successful for user %d", userId);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            LOG.warnf("Failed to parse reset token: %s", e.getMessage());
            throw new BadRequestException("Reset token không hợp lệ hoặc đã hết hạn", "RESET_TOKEN_INVALID");
        }
    }

    // ===================== PRIVATE HELPERS =====================

    /**
     * Sinh JWT token chứa userId, email, role.
     */
    private String generateToken(User user) {
        return Jwt.issuer(issuer)
                .subject(String.valueOf(user.id))
                .upn(user.email)
                .groups(Set.of(user.role.name()))
                .expiresIn(Duration.ofSeconds(tokenLifespan))
                .claim("full_name", user.fullName)
                .sign();
    }

    /**
     * Public wrapper — dùng cho AdminUserResource tạo staff.
     */
    public String hashPasswordPublic(String plainPassword) {
        return hashPassword(plainPassword);
    }

    /**
     * Hash password bằng BCrypt (Elytron).
     */
    private String hashPassword(String plainPassword) {
        try {
            PasswordFactory factory = PasswordFactory.getInstance(BCryptPassword.ALGORITHM_BCRYPT);
            // Generate BCrypt hash with cost factor 10
            org.wildfly.security.password.spec.IteratedSaltedPasswordAlgorithmSpec spec =
                    new org.wildfly.security.password.spec.IteratedSaltedPasswordAlgorithmSpec(
                            10, generateSalt());
            org.wildfly.security.password.spec.EncryptablePasswordSpec encSpec =
                    new org.wildfly.security.password.spec.EncryptablePasswordSpec(
                            plainPassword.toCharArray(), spec);
            BCryptPassword bcryptPwd = (BCryptPassword) factory.generatePassword(encSpec);
            return ModularCrypt.encodeAsString(bcryptPwd);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi mã hóa mật khẩu", e);
        }
    }

    /**
     * Verify password against BCrypt hash.
     */
    private boolean verifyPassword(String plainPassword, String hash) {
        try {
            PasswordFactory factory = PasswordFactory.getInstance(BCryptPassword.ALGORITHM_BCRYPT);
            Password decoded = factory.translate(ModularCrypt.decode(hash));
            return factory.verify(decoded, plainPassword.toCharArray());
        } catch (Exception e) {
            LOG.errorf(e, "Password verification failed");
            return false;
        }
    }

    /**
     * Generate 16-byte random salt.
     */
    private byte[] generateSalt() {
        byte[] salt = new byte[16];
        new java.security.SecureRandom().nextBytes(salt);
        return salt;
    }

    /**
     * Sinh OTP 6 chữ số ngẫu nhiên.
     */
    private String generateOtp() {
        int otp = new java.security.SecureRandom().nextInt(900000) + 100000;
        return String.valueOf(otp);
    }
}
