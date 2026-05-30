package com.monowear.dto.auth;

/**
 * Response trả về sau khi đăng nhập/đăng ký thành công.
 *
 * @param token     JWT access token
 * @param tokenType Loại token (Bearer)
 * @param expiresIn Thời gian hết hạn (seconds)
 * @param user      Thông tin user
 */
public record AuthResponse(
        String token,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
    public static AuthResponse of(String token, long expiresIn, UserResponse user) {
        return new AuthResponse(token, "Bearer", expiresIn, user);
    }
}
