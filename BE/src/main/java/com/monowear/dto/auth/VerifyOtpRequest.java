package com.monowear.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyOtpRequest(
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        String email,

        @NotBlank(message = "Mã OTP không được để trống")
        @Pattern(regexp = "\\d{6}", message = "Mã OTP phải gồm 6 chữ số")
        String code
) {}
