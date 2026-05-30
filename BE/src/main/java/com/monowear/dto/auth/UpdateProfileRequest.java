package com.monowear.dto.auth;

import jakarta.validation.constraints.Size;

/**
 * DTO cập nhật hồ sơ cá nhân (Customer tự update).
 */
public record UpdateProfileRequest(
        @Size(max = 100) String fullName,
        @Size(max = 20) String phoneNumber,
        @Size(max = 500) String address
) {}
