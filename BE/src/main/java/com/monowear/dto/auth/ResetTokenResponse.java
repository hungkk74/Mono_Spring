package com.monowear.dto.auth;

public record ResetTokenResponse(
        String resetToken,
        long expiresIn   // seconds
) {
    public static ResetTokenResponse of(String token, long expiresIn) {
        return new ResetTokenResponse(token, expiresIn);
    }
}
